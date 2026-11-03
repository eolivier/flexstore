-- The failed sign-ins in a row of an account (a wrong password): at most one row per account, which only exists
-- while the count is above 0. At the limit (5), the account is locked; a successful sign-in deletes the row
CREATE TABLE failed_sign_in (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    attempt_count INT NOT NULL CHECK (attempt_count > 0),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
