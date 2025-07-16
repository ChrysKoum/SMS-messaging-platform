package com.intercom.sms.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intercom.sms.domain.model.Message;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Producer for sending SMS requests to Kafka
 */
@ApplicationScoped
public class MessageProducer {

    private static final Logger LOG = Logger.getLogger(MessageProducer.class);

    @Inject
    @Channel("sms-requests")
    Emitter<String> smsRequestEmitter;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Send SMS request to Kafka topic for processing
     */
    public void sendSmsRequest(Message message) {
        try {
            // Create message payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("messageId", message.getId().toString());
            payload.put("sender", message.getSender());
            payload.put("recipient", message.getRecipient());
            payload.put("text", message.getText());
            payload.put("status", message.getStatus().name());
            payload.put("createdAt", message.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // Convert to JSON
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            LOG.infof("Sending SMS request to Kafka: messageId=%s", message.getId());
            LOG.debugf("SMS request payload: %s", jsonPayload);
            
            // Send to Kafka
            smsRequestEmitter.send(jsonPayload);
            
            LOG.infof("SMS request sent successfully: messageId=%s", message.getId());
            
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send SMS request to Kafka: messageId=%s", message.getId());
            throw new RuntimeException("Failed to send SMS request to message broker", e);
        }
    }

    /**
     * Send a retry request for failed messages
     */
    public void sendRetryRequest(Message message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("messageId", message.getId().toString());
            payload.put("sender", message.getSender());
            payload.put("recipient", message.getRecipient());
            payload.put("text", message.getText());
            payload.put("status", message.getStatus().name());
            payload.put("createdAt", message.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            payload.put("isRetry", true);
            payload.put("retryAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            LOG.infof("Sending SMS retry request to Kafka: messageId=%s", message.getId());
            
            smsRequestEmitter.send(jsonPayload);
            
            LOG.infof("SMS retry request sent successfully: messageId=%s", message.getId());
            
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send SMS retry request to Kafka: messageId=%s", message.getId());
            throw new RuntimeException("Failed to send SMS retry request to message broker", e);
        }
    }
}
