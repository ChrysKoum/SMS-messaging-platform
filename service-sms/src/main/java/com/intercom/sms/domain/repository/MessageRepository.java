package com.intercom.sms.domain.repository;

import com.intercom.sms.domain.model.Message;
import com.intercom.sms.domain.model.MessageStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Message entities using Panache
 */
@ApplicationScoped
public class MessageRepository implements PanacheRepository<Message> {

    /**
     * Find message by ID
     */
    public Optional<Message> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    /**
     * Find all messages for a specific user (sender or recipient)
     */
    public List<Message> findByUser(String phoneNumber, Page page) {
        return find("sender = ?1 or recipient = ?1", 
                   Sort.by("createdAt").descending(), 
                   phoneNumber)
                .page(page)
                .list();
    }

    /**
     * Find messages by sender
     */
    public List<Message> findBySender(String sender, Page page) {
        return find("sender", Sort.by("createdAt").descending(), sender)
                .page(page)
                .list();
    }

    /**
     * Find messages by recipient
     */
    public List<Message> findByRecipient(String recipient, Page page) {
        return find("recipient", Sort.by("createdAt").descending(), recipient)
                .page(page)
                .list();
    }

    /**
     * Find messages by status
     */
    public List<Message> findByStatus(MessageStatus status, Page page) {
        return find("status", Sort.by("createdAt").descending(), status)
                .page(page)
                .list();
    }

    /**
     * Find messages by status and user
     */
    public List<Message> findByStatusAndUser(MessageStatus status, String phoneNumber, Page page) {
        return find("status = ?1 and (sender = ?2 or recipient = ?2)", 
                   Sort.by("createdAt").descending(), 
                   status, phoneNumber)
                .page(page)
                .list();
    }

    /**
     * Find messages created between dates
     */
    public List<Message> findByDateRange(LocalDateTime from, LocalDateTime to, Page page) {
        return find("createdAt >= ?1 and createdAt <= ?2", 
                   Sort.by("createdAt").descending(), 
                   from, to)
                .page(page)
                .list();
    }

    /**
     * Count messages for a user
     */
    public long countByUser(String phoneNumber) {
        return count("sender = ?1 or recipient = ?1", phoneNumber);
    }

    /**
     * Count messages by status
     */
    public long countByStatus(MessageStatus status) {
        return count("status", status);
    }

    /**
     * Find pending messages (for potential retry scenarios)
     */
    public List<Message> findPendingMessages(int limit) {
        return find("status", Sort.by("createdAt"), MessageStatus.PENDING)
                .page(Page.ofSize(limit))
                .list();
    }

    /**
     * Find failed messages for analysis
     */
    public List<Message> findFailedMessages(Page page) {
        return find("status", Sort.by("updatedAt").descending(), MessageStatus.FAILED)
                .page(page)
                .list();
    }

    /**
     * Update message status
     */
    public void updateStatus(UUID messageId, MessageStatus status, String failureReason) {
        update("status = ?1, failureReason = ?2, updatedAt = current_timestamp where id = ?3", 
               status, failureReason, messageId);
    }

    /**
     * Bulk update status for multiple messages
     */
    public void updateStatusBulk(List<UUID> messageIds, MessageStatus status) {
        update("status = ?1, updatedAt = current_timestamp where id in ?2", 
               status, messageIds);
    }
}
