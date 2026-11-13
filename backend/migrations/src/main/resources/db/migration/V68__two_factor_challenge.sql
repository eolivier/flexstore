-- A sign-in whose password was right waits for the second factor: its challenge is a short-lived reference
-- (5 minutes) to the user, found by its id and deleted once the code is verified. It was kept in the memory of
-- the application, so a restart dropped the challenges in progress and two instances did not share them: it is
-- now a table. A challenge cannot outlive its user
CREATE TABLE two_factor_challenge (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX two_factor_challenge_user_id_idx ON two_factor_challenge(user_id);
