package com.intercom.processor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intercom.processor.dto.SmsRequestMessage;
import com.intercom.processor.service.ProcessingService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * Kafka consumer for SMS processing requests
 */
@ApplicationScoped
public class SmsRequestConsumer {

    private static final Logger LOG = Logger.getLogger(SmsRequestConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ProcessingService processingService;

    /**
     * Consume SMS requests from Kafka and process them
     */
    @Incoming("sms-requests")
    @Timed(value = "sms_processing_duration", description = "Time taken to process SMS messages")
    @Counted(value = "sms_processing_attempts", description = "Number of SMS processing attempts")
    public void processSmsRequest(String message) {
        LOG.infof("Received SMS request message: %s", message);
        
        try {
            // Parse the message
            SmsRequestMessage smsRequest = objectMapper.readValue(message, SmsRequestMessage.class);
            LOG.infof("Processing SMS request for message ID: %s", smsRequest.getMessageId());
            
            // Process the message asynchronously
            processingService.processMessage(smsRequest);
            
        } catch (Exception e) {
            LOG.errorf(e, "Failed to process SMS request message: %s", message);
            // In a real implementation, this could be sent to a dead letter queue
        }
    }
}
