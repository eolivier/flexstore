-- An order is delivered to an address and billed to an address (often the same), both addresses of the customer:
-- foreign keys to address, kept while an order refers to them (no ON DELETE CASCADE)
ALTER TABLE customer_order
    ADD COLUMN delivery_address_id VARCHAR(50) REFERENCES address(id),
    ADD COLUMN billing_address_id VARCHAR(50) REFERENCES address(id);

-- The orders placed before are delivered and billed to the first address of their customer
UPDATE customer_order SET
    delivery_address_id = (SELECT address.id FROM address WHERE address.user_id = customer_order.user_id ORDER BY address.position LIMIT 1),
    billing_address_id = (SELECT address.id FROM address WHERE address.user_id = customer_order.user_id ORDER BY address.position LIMIT 1);

-- The orders of a customer without any address (an account created by an administrator) cannot have one:
-- they are removed, with their lines
DELETE FROM customer_order WHERE delivery_address_id IS NULL;

ALTER TABLE customer_order
    ALTER COLUMN delivery_address_id SET NOT NULL,
    ALTER COLUMN billing_address_id SET NOT NULL;
