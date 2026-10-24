-- The status of the account: the existing users are active, a customer who creates their account waits for the
-- verification of their email (PENDING_VERIFICATION)
ALTER TABLE "user" ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'LOCKED', 'DISABLED'));

-- The phone number given by a customer who creates their account (none for the accounts created by an administrator)
ALTER TABLE "user" ADD COLUMN phone_number VARCHAR(30);

-- The postal addresses of a user: a user may have several, the first one is given when the account is created
CREATE TABLE address (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    position INT NOT NULL,
    street VARCHAR(255) NOT NULL,
    complement VARCHAR(255),
    postal_code VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX address_user_id_idx ON address(user_id);
