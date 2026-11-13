-- The sessions of the signed-in users (the table refresh_session, V44) are user_token rows of a third type,
-- REFRESH_SESSION: like the links sent by email, a session is the hash of a secret token of a user, which
-- expires. A session also has revoked_at, set when its refresh token is used or the user signs out, which no
-- other type has. Unlike the password reset links, a user may have several sessions at once
ALTER TABLE user_token DROP CONSTRAINT user_token_type_check;
ALTER TABLE user_token ADD CONSTRAINT user_token_type_check
    CHECK (type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'REFRESH_SESSION'));
ALTER TABLE user_token ADD COLUMN revoked_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE user_token ADD CHECK (revoked_at IS NULL OR type = 'REFRESH_SESSION');

INSERT INTO user_token (id, type, token_hash, user_id, expires_at, revoked_at, created_at, updated_at)
SELECT id, 'REFRESH_SESSION', token_hash, user_id, expires_at, revoked_at, created_at, updated_at
FROM refresh_session;

DROP TABLE refresh_session;
