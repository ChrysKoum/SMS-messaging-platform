-- Create messages table
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender VARCHAR(20) NOT NULL,
    recipient VARCHAR(20) NOT NULL,
    text VARCHAR(1600) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_messages_sender ON messages(sender);
CREATE INDEX idx_messages_recipient ON messages(recipient);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_created_at ON messages(created_at);
CREATE INDEX idx_messages_updated_at ON messages(updated_at);

-- Composite index for user queries (sender or recipient)
CREATE INDEX idx_messages_user_created ON messages(sender, created_at DESC);
CREATE INDEX idx_messages_user_created_2 ON messages(recipient, created_at DESC);

-- Index for status and user combination
CREATE INDEX idx_messages_status_user ON messages(status, sender);
CREATE INDEX idx_messages_status_user_2 ON messages(status, recipient);

-- Add constraint for status values
ALTER TABLE messages ADD CONSTRAINT chk_status 
    CHECK (status IN ('PENDING', 'SENT', 'FAILED'));

-- Add constraint for phone number format (basic validation)
ALTER TABLE messages ADD CONSTRAINT chk_sender_format 
    CHECK (sender ~ '^\+?[1-9]\d{1,14}$');
    
ALTER TABLE messages ADD CONSTRAINT chk_recipient_format 
    CHECK (recipient ~ '^\+?[1-9]\d{1,14}$');

-- Add constraint for text length
ALTER TABLE messages ADD CONSTRAINT chk_text_length 
    CHECK (length(text) >= 1 AND length(text) <= 1600);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_messages_updated_at 
    BEFORE UPDATE ON messages 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
