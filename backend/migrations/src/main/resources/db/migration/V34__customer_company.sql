-- The company a customer buys for, given when they create their account: its name, its SIREN (9 digits) and its
-- intra-community VAT number when it gives one. At most one per user; deleting the user deletes it
CREATE TABLE customer_company (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    siren VARCHAR(9) NOT NULL CHECK (siren ~ '^[0-9]{9}$'),
    vat_number VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
