package com.intercom.processor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing an SMS request message from Kafka
 */
public class SmsRequestMessage {

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("sender")
    private String sender;

    @JsonProperty("recipient")
    private String recipient;

    @JsonProperty("text")
    private String text;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("isRetry")
    private boolean isRetry = false;

    @JsonProperty("retryAt")
    private String retryAt;

    // Constructors
    public SmsRequestMessage() {
    }

    public SmsRequestMessage(String messageId, String sender, String recipient, String text) {
        this.messageId = messageId;
        this.sender = sender;
        this.recipient = recipient;
        this.text = text;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public void setRetry(boolean retry) {
        isRetry = retry;
    }

    public String getRetryAt() {
        return retryAt;
    }

    public void setRetryAt(String retryAt) {
        this.retryAt = retryAt;
    }

    @Override
    public String toString() {
        return "SmsRequestMessage{" +
                "messageId='" + messageId + '\'' +
                ", sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", textLength=" + (text != null ? text.length() : 0) +
                ", status='" + status + '\'' +
                ", isRetry=" + isRetry +
                '}';
    }
}
