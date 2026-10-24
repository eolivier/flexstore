-- The status of the account: the existing users are active, a customer who creates their account waits for the
-- verification of their email (PENDING_VERIFICATION)
ALTER TABLE "user" ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'LOCKED', 'DISABLED'));

-- The phone number and the postal address given by a customer who creates their account
-- (none for the accounts created by an administrator)
ALTER TABLE "user" ADD COLUMN phone_number VARCHAR(30);
ALTER TABLE "user" ADD COLUMN street VARCHAR(255);
ALTER TABLE "user" ADD COLUMN address_complement VARCHAR(255);
ALTER TABLE "user" ADD COLUMN postal_code VARCHAR(20);
ALTER TABLE "user" ADD COLUMN city VARCHAR(100);
ALTER TABLE "user" ADD COLUMN country VARCHAR(100);
