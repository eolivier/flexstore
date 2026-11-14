-- The customers who wait for a product out of stock, to be notified by email when it is back: at most once per
-- customer and product. The subscription ends with the email. Deleting the product or the customer deletes the
-- subscriptions. The moment of the subscription is the creation date of the row
CREATE TABLE restock_subscription (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    product_id VARCHAR(50) NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, product_id)
);

CREATE INDEX restock_subscription_product_id_idx ON restock_subscription(product_id);
