package com.intercom.processor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for delivery report callback to SMS service
 */
public class DeliveryReportRequest {

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("failure_reason")
    private String failureReason;

    @JsonProperty("processed_at")
    private String processedAt;

    // Constructors
    public DeliveryReportRequest() {
    }

    public DeliveryReportRequest(String messageId, String status, String failureReason) {
        this.messageId = messageId;
        this.status = status;
        this.failureReason = failureReason;
        this.processedAt = java.time.LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
                "messageId='" + messageId + '\'' +
                ", status='" + status + '\'' +
                ", failureReason='" + failureReason + '\'' +
                ", processedAt='" + processedAt + '\'' +
                '}';
    }
}
