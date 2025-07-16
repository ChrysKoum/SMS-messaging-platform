package com.intercom.sms.service;

import com.intercom.sms.api.dto.*;
import com.intercom.sms.domain.model.Message;
import com.intercom.sms.domain.model.MessageStatus;
import com.intercom.sms.domain.repository.MessageRepository;
import com.intercom.sms.messaging.MessageProducer;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for SMS message operations
 */
@ApplicationScoped
public class MessageService {

    private static final Logger LOG = Logger.getLogger(MessageService.class);

    @Inject
    MessageRepository messageRepository;

    @Inject
    MessageProducer messageProducer;

    @Inject
    MeterRegistry meterRegistry;

    /**
     * Send a new SMS message
     */
    @Transactional
    @Timed(value = "sms_send_duration", description = "Time taken to send SMS message")
    @Counted(value = "sms_send_attempts", description = "Number of SMS send attempts")
    public MessageResponse sendMessage(SendMessageRequest request) {
        LOG.infof("Sending SMS from %s to %s", request.getSender(), request.getRecipient());
        
        // Create and persist message
        Message message = new Message(request.getSender(), request.getRecipient(), request.getText());
        messageRepository.persist(message);
        
        LOG.infof("Message persisted with ID: %s", message.getId());
        
        // Send to message broker for processing
        try {
            messageProducer.sendSmsRequest(message);
            LOG.infof("Message %s sent to processing queue", message.getId());
            
            // Increment success counter
            meterRegistry.counter("sms_queued_total").increment();
            
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send message %s to processing queue", message.getId());
            // Mark as failed if we can't queue it
            message.markAsFailed("Failed to queue message for processing: " + e.getMessage());
            messageRepository.persist(message);
            
            // Increment failure counter
            meterRegistry.counter("sms_queue_failed_total").increment();
        }
        
        return mapToResponse(message);
    }

    /**
     * Get a message by ID
     */
    public Optional<MessageResponse> getMessage(UUID messageId) {
        LOG.debugf("Retrieving message with ID: %s", messageId);
        
        return messageRepository.findByIdOptional(messageId)
                .map(this::mapToResponse);
    }

    /**
     * Get messages for a specific user with pagination
     */
    public MessageListResponse getUserMessages(String userId, int page, int size, MessageStatus status) {
        LOG.debugf("Retrieving messages for user %s, page %d, size %d, status %s", 
                  userId, page, size, status);
        
        Page pageRequest = Page.of(page, size);
        List<Message> messages;
        long totalCount;
        
        if (status != null) {
            messages = messageRepository.findByStatusAndUser(status, userId, pageRequest);
            totalCount = messageRepository.countByStatus(status);
        } else {
            messages = messageRepository.findByUser(userId, pageRequest);
            totalCount = messageRepository.countByUser(userId);
        }
        
        List<MessageResponse> messageResponses = messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new MessageListResponse(messageResponses, totalCount, page, size);
    }

    /**
     * Process delivery report from processor service
     */
    @Transactional
    @Timed(value = "sms_callback_duration", description = "Time taken to process delivery callbacks")
    public boolean processDeliveryReport(DeliveryReportRequest deliveryReport) {
        LOG.infof("Processing delivery report for message %s with status %s", 
                 deliveryReport.getMessageId(), deliveryReport.getStatus());
        
        Optional<Message> messageOpt = messageRepository.findByIdOptional(deliveryReport.getMessageId());
        
        if (messageOpt.isEmpty()) {
            LOG.warnf("Message not found for delivery report: %s", deliveryReport.getMessageId());
            return false;
        }
        
        Message message = messageOpt.get();
        
        // Update message status based on delivery report
        switch (deliveryReport.getStatus()) {
            case SENT:
                message.markAsSent();
                LOG.infof("Message %s marked as SENT", message.getId());
                
                // Increment success counter
                meterRegistry.counter("sms_sent_total").increment();
                break;
            case FAILED:
                message.markAsFailed(deliveryReport.getFailureReason());
                LOG.infof("Message %s marked as FAILED: %s", message.getId(), deliveryReport.getFailureReason());
                
                // Increment failure counter
                meterRegistry.counter("sms_failed_total").increment();
                break;
            default:
                LOG.warnf("Unexpected status in delivery report: %s", deliveryReport.getStatus());
                return false;
        }
        
        messageRepository.persist(message);
        return true;
    }

    /**
     * Get message statistics
     */
    public MessageStatsResponse getMessageStats() {
        long pendingCount = messageRepository.countByStatus(MessageStatus.PENDING);
        long sentCount = messageRepository.countByStatus(MessageStatus.SENT);
        long failedCount = messageRepository.countByStatus(MessageStatus.FAILED);
        long totalCount = messageRepository.count();
        
        return new MessageStatsResponse(totalCount, pendingCount, sentCount, failedCount);
    }

    /**
     * Get failed messages for analysis
     */
    public MessageListResponse getFailedMessages(int page, int size) {
        Page pageRequest = Page.of(page, size);
        List<Message> messages = messageRepository.findFailedMessages(pageRequest);
        long totalCount = messageRepository.countByStatus(MessageStatus.FAILED);
        
        List<MessageResponse> messageResponses = messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new MessageListResponse(messageResponses, totalCount, page, size);
    }

    /**
     * Map Message entity to MessageResponse DTO
     */
    private MessageResponse mapToResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSender(),
                message.getRecipient(),
                message.getText(),
                message.getStatus(),
                message.getFailureReason(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}
