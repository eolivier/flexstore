-- The verification links sent to the customers who create their account (valid for 1 hour).
-- Only a hash of the token is stored: the link cannot be rebuilt from the database
CREATE TABLE email_verification (
    token_hash VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX email_verification_user_id_idx ON email_verification(user_id);
