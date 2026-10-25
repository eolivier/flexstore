-- An address has its own recipient, who may be someone else than the user (a gift, a company),
-- and belongs to no user: the users refer to their addresses through user_address, the orders through their
-- delivery and billing addresses

-- The recipient of the existing addresses is their user
ALTER TABLE address
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255),
    ADD COLUMN company_name VARCHAR(255);

UPDATE address SET first_name = "user".first_name, last_name = "user".last_name
FROM "user" WHERE "user".id = address.user_id;

ALTER TABLE address
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL;

-- The addresses of a user, in the order they were given
CREATE TABLE user_address (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    address_id VARCHAR(50) NOT NULL REFERENCES address(id),
    position INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, address_id)
);

CREATE INDEX user_address_user_id_idx ON user_address(user_id);

INSERT INTO user_address (id, user_id, address_id, position, created_at, updated_at)
SELECT gen_random_uuid()::text, user_id, id, position, created_at, updated_at FROM address;

ALTER TABLE address DROP COLUMN user_id, DROP COLUMN position;
