-- The sessions of the signed-in users: a refresh token renews the access of a user once their access token
-- expired (valid for 7 days). A refresh token works once: it is revoked when it is used (revoked_at is set), and
-- replaced by the one of the next session. Only a hash of the token is stored, as for the verification links.
-- Deleting a user deletes their sessions
CREATE TABLE refresh_session (
    id VARCHAR(50) PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(50) NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX refresh_session_user_id_idx ON refresh_session(user_id);
