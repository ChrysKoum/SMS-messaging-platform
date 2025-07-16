package com.intercom.sms.domain.model;

/**
 * Enumeration representing the possible states of an SMS message
 */
public enum MessageStatus {
    /**
     * Message has been received and validated, awaiting processing
     */
    PENDING,
    
    /**
     * Message has been successfully sent to the recipient
     */
    SENT,
    
    /**
     * Message delivery failed
     */
    FAILED
}
