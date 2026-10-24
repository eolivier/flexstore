-- An order keeps a copy of its delivery address and of its billing address (often the same).
-- The orders placed before have none: the columns are nullable
ALTER TABLE customer_order
    ADD COLUMN delivery_street VARCHAR(255),
    ADD COLUMN delivery_complement VARCHAR(255),
    ADD COLUMN delivery_postal_code VARCHAR(20),
    ADD COLUMN delivery_city VARCHAR(100),
    ADD COLUMN delivery_country VARCHAR(100),
    ADD COLUMN billing_street VARCHAR(255),
    ADD COLUMN billing_complement VARCHAR(255),
    ADD COLUMN billing_postal_code VARCHAR(20),
    ADD COLUMN billing_city VARCHAR(100),
    ADD COLUMN billing_country VARCHAR(100);
