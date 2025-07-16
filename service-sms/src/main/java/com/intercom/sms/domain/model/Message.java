package com.intercom.sms.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SMS Message Entity
 * Represents an SMS message in the system with all its lifecycle states
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_sender", columnList = "sender"),
    @Index(name = "idx_recipient", columnList = "recipient"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Sender phone number is required")
    @Pattern(regexp = "\\+?[1-9]\\d{1,14}", message = "Invalid phone number format")
    @Column(name = "sender", nullable = false, length = 20)
    private String sender;

    @NotBlank(message = "Recipient phone number is required")
    @Pattern(regexp = "\\+?[1-9]\\d{1,14}", message = "Invalid phone number format")
    @Column(name = "recipient", nullable = false, length = 20)
    private String recipient;

    @NotBlank(message = "Message text is required")
    @Size(min = 1, max = 1600, message = "Message text must be between 1 and 1600 characters")
    @Column(name = "text", nullable = false, length = 1600)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status = MessageStatus.PENDING;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Message() {
    }

    public Message(String sender, String recipient, String text) {
        this.sender = sender;
        this.recipient = recipient;
        this.text = text;
        this.status = MessageStatus.PENDING;
    }

    // Business methods
    public void markAsSent() {
        this.status = MessageStatus.SENT;
        this.failureReason = null;
    }

    public void markAsFailed(String reason) {
        this.status = MessageStatus.FAILED;
        this.failureReason = reason;
    }

    public boolean isPending() {
        return status == MessageStatus.PENDING;
    }

    public boolean isSent() {
        return status == MessageStatus.SENT;
    }

    public boolean isFailed() {
        return status == MessageStatus.FAILED;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message message)) return false;
        return id != null && id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
