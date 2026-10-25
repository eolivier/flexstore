-- The reset of the password of a customer who forgot it: the link sent by email is valid for 1 hour, and only
-- the last link asked works (one row per user at most)
CREATE TABLE password_reset (
    id VARCHAR(50) PRIMARY KEY,
    -- SHA-256 of the token sent by email, never the token itself
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(50) NOT NULL UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
