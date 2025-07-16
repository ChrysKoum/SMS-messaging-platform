package com.intercom.processor.service;

import com.intercom.processor.client.SmsServiceClient;
import com.intercom.processor.dto.DeliveryReportRequest;
import com.intercom.processor.dto.SmsRequestMessage;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for processing SMS messages and simulating delivery
 */
@ApplicationScoped
public class ProcessingService {

    private static final Logger LOG = Logger.getLogger(ProcessingService.class);

    @Inject
    @RestClient
    SmsServiceClient smsServiceClient;

    @Inject
    MeterRegistry meterRegistry;

    @ConfigProperty(name = "processor.simulation.min-delay-ms", defaultValue = "500")
    int minDelayMs;

    @ConfigProperty(name = "processor.simulation.max-delay-ms", defaultValue = "2000")
    int maxDelayMs;

    @ConfigProperty(name = "processor.simulation.success-rate", defaultValue = "0.8")
    double successRate;

    /**
     * Process an SMS message asynchronously
     */
    public void processMessage(SmsRequestMessage smsRequest) {
        LOG.infof("Starting processing for message ID: %s", smsRequest.getMessageId());
        
        // Process asynchronously to avoid blocking the Kafka consumer
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate processing delay
                int delay = ThreadLocalRandom.current().nextInt(minDelayMs, maxDelayMs + 1);
                Thread.sleep(delay);
                
                // Simulate success/failure based on configured success rate
                boolean isSuccess = ThreadLocalRandom.current().nextDouble() < successRate;
                
                // Create delivery report
                DeliveryReportRequest deliveryReport;
                if (isSuccess) {
                    deliveryReport = new DeliveryReportRequest(
                        smsRequest.getMessageId(),
                        "SENT",
                        null
                    );
                    LOG.infof("Message %s processed successfully", smsRequest.getMessageId());
                    meterRegistry.counter("sms_processing_success_total").increment();
                } else {
                    String failureReason = generateRandomFailureReason();
                    deliveryReport = new DeliveryReportRequest(
                        smsRequest.getMessageId(),
                        "FAILED",
                        failureReason
                    );
                    LOG.infof("Message %s processing failed: %s", smsRequest.getMessageId(), failureReason);
                    meterRegistry.counter("sms_processing_failure_total").increment();
                }
                
                // Send callback to SMS service
                sendDeliveryReport(deliveryReport);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.errorf("Message processing interrupted for ID: %s", smsRequest.getMessageId());
            } catch (Exception e) {
                LOG.errorf(e, "Unexpected error processing message ID: %s", smsRequest.getMessageId());
                meterRegistry.counter("sms_processing_error_total").increment();
            }
        });
    }

    /**
     * Send delivery report back to SMS service
     */
    private void sendDeliveryReport(DeliveryReportRequest deliveryReport) {
        try {
            LOG.infof("Sending delivery report for message %s with status %s", 
                     deliveryReport.getMessageId(), deliveryReport.getStatus());
            
            smsServiceClient.sendDeliveryReport(deliveryReport);
            
            LOG.infof("Delivery report sent successfully for message %s", deliveryReport.getMessageId());
            meterRegistry.counter("callback_success_total").increment();
            
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send delivery report for message %s", deliveryReport.getMessageId());
            meterRegistry.counter("callback_failure_total").increment();
            
            // In a real implementation, this could trigger retry logic
        }
    }

    /**
     * Generate random failure reasons for simulation
     */
    private String generateRandomFailureReason() {
        String[] reasons = {
            "Invalid phone number",
            "Network timeout",
            "Carrier rejected message",
            "Daily quota exceeded",
            "Phone number blocked",
            "Message content violation",
            "Temporary service unavailable"
        };
        
        int index = ThreadLocalRandom.current().nextInt(reasons.length);
        return reasons[index];
    }
}
