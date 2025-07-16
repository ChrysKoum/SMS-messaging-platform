package com.intercom.sms.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for sending SMS messages
 */
public class SendMessageRequest {

    @NotBlank(message = "Sender phone number is required")
    @Pattern(regexp = "\\+?[1-9]\\d{1,14}", 
             message = "Invalid sender phone number format. Use international format (e.g., +1234567890)")
    @JsonProperty("sender")
    private String sender;

    @NotBlank(message = "Recipient phone number is required")
    @Pattern(regexp = "\\+?[1-9]\\d{1,14}", 
             message = "Invalid recipient phone number format. Use international format (e.g., +1234567890)")
    @JsonProperty("recipient")
    private String recipient;

    @NotBlank(message = "Message text is required")
    @Size(min = 1, max = 1600, message = "Message text must be between 1 and 1600 characters")
    @JsonProperty("text")
    private String text;

    // Constructors
    public SendMessageRequest() {
    }

    public SendMessageRequest(String sender, String recipient, String text) {
        this.sender = sender;
        this.recipient = recipient;
        this.text = text;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "SendMessageRequest{" +
                "sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", textLength=" + (text != null ? text.length() : 0) +
                '}';
    }
}
