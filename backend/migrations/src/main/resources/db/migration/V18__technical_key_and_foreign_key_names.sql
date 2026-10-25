-- Every table has a technical primary key id, and a foreign key is named after the table it references
-- (see the database-convention skill)

-- email_verification: the hash of the token is no longer the primary key, it stays unique;
-- a user may have several links over time, each one references its user (user_id, unchanged)
ALTER TABLE email_verification ADD COLUMN id VARCHAR(50);
UPDATE email_verification SET id = gen_random_uuid()::text;
ALTER TABLE email_verification ALTER COLUMN id SET NOT NULL;
ALTER TABLE email_verification DROP CONSTRAINT email_verification_pkey;
ALTER TABLE email_verification ADD CONSTRAINT email_verification_pkey PRIMARY KEY (id);
ALTER TABLE email_verification ADD CONSTRAINT email_verification_token_hash_key UNIQUE (token_hash);

-- customer_order_line: order_id references customer_order, it is named after it
ALTER TABLE customer_order_line RENAME COLUMN order_id TO customer_order_id;
ALTER TABLE customer_order_line
    RENAME CONSTRAINT customer_order_line_order_id_fkey TO customer_order_line_customer_order_id_fkey;
ALTER TABLE customer_order_line
    RENAME CONSTRAINT customer_order_line_order_id_position_key TO customer_order_line_customer_order_id_position_key;
