package com.intercom.sms.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intercom.sms.domain.model.MessageStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for delivery report callbacks from processor service
 */
public class DeliveryReportRequest {

    @NotNull(message = "Message ID is required")
    @JsonProperty("message_id")
    private UUID messageId;

    @NotNull(message = "Status is required")
    @JsonProperty("status")
    private MessageStatus status;

    @JsonProperty("failure_reason")
    private String failureReason;

    @JsonProperty("processed_at")
    private String processedAt;

    // Constructors
    public DeliveryReportRequest() {
    }

    public DeliveryReportRequest(UUID messageId, MessageStatus status, String failureReason) {
        this.messageId = messageId;
        this.status = status;
        this.failureReason = failureReason;
    }

    // Getters and Setters
    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }

    @Override
    public String toString() {
        return "DeliveryReportRequest{" +
                "messageId=" + messageId +
                ", status=" + status +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}
