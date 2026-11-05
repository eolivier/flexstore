-- The links sent by email to a user (verification of the email, new password) share one table: the type tells
-- the operation. Only a hash of the token is stored: the link cannot be rebuilt from the database
CREATE TABLE user_token (
    id VARCHAR(50) PRIMARY KEY,
    -- EMAIL_VERIFICATION or PASSWORD_RESET
    type VARCHAR(30) NOT NULL,
    -- SHA-256 of the token sent by email, never the token itself
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(50) NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET'))
);

CREATE INDEX user_token_user_id_idx ON user_token(user_id);

-- Only the last password reset link asked works: one per user at most
-- (a user may have several verification links)
CREATE UNIQUE INDEX user_token_password_reset_user_id_key ON user_token(user_id) WHERE type = 'PASSWORD_RESET';

INSERT INTO user_token (id, type, token_hash, user_id, expires_at, created_at, updated_at)
SELECT id, 'EMAIL_VERIFICATION', token_hash, user_id, expires_at, created_at, updated_at FROM email_verification;

INSERT INTO user_token (id, type, token_hash, user_id, expires_at, created_at, updated_at)
SELECT id, 'PASSWORD_RESET', token_hash, user_id, expires_at, created_at, updated_at FROM password_reset;

DROP TABLE email_verification;
DROP TABLE password_reset;
