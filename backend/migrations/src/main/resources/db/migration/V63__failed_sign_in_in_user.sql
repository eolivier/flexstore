-- The failed sign-ins in a row of an account are a column of the user, as a user has at most one count (the
-- table failed_sign_in, V43, had at most one row per user, only while the count was above 0): 0 for an account
-- without failed sign-in, 1 to 4 otherwise. At the limit (5), the account is locked and the count starts again
ALTER TABLE "user" ADD COLUMN failed_sign_in_count INT NOT NULL DEFAULT 0 CHECK (failed_sign_in_count >= 0);

UPDATE "user"
SET failed_sign_in_count = failed_sign_in.attempt_count
FROM failed_sign_in
WHERE failed_sign_in.user_id = "user".id;

DROP TABLE failed_sign_in;
