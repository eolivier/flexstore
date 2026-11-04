-- Two-factor authentication (TOTP), restricted to an ADMIN account in this iteration. Null while disabled; the
-- secret is kept once an enrollment starts (status PENDING) until the first code confirms it (status ENABLED).
-- The domain rebuilds its sealed TwoFactorAuthentication from null-or-not at the persistence adapter boundary.
ALTER TABLE "user" ADD COLUMN two_factor_secret VARCHAR(64);
ALTER TABLE "user" ADD COLUMN two_factor_status VARCHAR(20) CHECK (two_factor_status IN ('PENDING', 'ENABLED'));
ALTER TABLE "user" ADD CONSTRAINT user_two_factor_consistency
    CHECK ((two_factor_secret IS NULL) = (two_factor_status IS NULL));
