package com.intercom.sms.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for message statistics
 */
public class MessageStatsResponse {

    @JsonProperty("total_messages")
    private long totalMessages;

    @JsonProperty("pending_messages")
    private long pendingMessages;

    @JsonProperty("sent_messages")
    private long sentMessages;

    @JsonProperty("failed_messages")
    private long failedMessages;

    @JsonProperty("success_rate")
    private double successRate;

    // Constructors
    public MessageStatsResponse() {
    }

    public MessageStatsResponse(long totalMessages, long pendingMessages, 
                               long sentMessages, long failedMessages) {
        this.totalMessages = totalMessages;
        this.pendingMessages = pendingMessages;
        this.sentMessages = sentMessages;
        this.failedMessages = failedMessages;
        
        // Calculate success rate
        long processedMessages = sentMessages + failedMessages;
        this.successRate = processedMessages > 0 ? (double) sentMessages / processedMessages : 0.0;
    }

    // Getters and Setters
    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public long getPendingMessages() {
        return pendingMessages;
    }

    public void setPendingMessages(long pendingMessages) {
        this.pendingMessages = pendingMessages;
    }

    public long getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(long sentMessages) {
        this.sentMessages = sentMessages;
    }

    public long getFailedMessages() {
        return failedMessages;
    }

    public void setFailedMessages(long failedMessages) {
        this.failedMessages = failedMessages;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }
}
