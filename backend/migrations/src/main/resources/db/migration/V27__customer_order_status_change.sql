-- The statuses an order went through, with the moment it reached each one. customer_order.status keeps the last
-- one, to read the orders without their history
CREATE TABLE customer_order_status_change (
    id VARCHAR(50) PRIMARY KEY,
    customer_order_id VARCHAR(50) NOT NULL REFERENCES customer_order(id) ON DELETE CASCADE,
    position INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (customer_order_id, position)
);

CREATE INDEX customer_order_status_change_customer_order_id_idx
    ON customer_order_status_change(customer_order_id);

-- The orders already placed: placed awaiting their payment, then their current status at their last update (the
-- statuses in between were not kept)
INSERT INTO customer_order_status_change (id, customer_order_id, position, status, changed_at)
SELECT gen_random_uuid()::text, id, 0, 'AWAITING_PAYMENT', placed_at FROM customer_order;

INSERT INTO customer_order_status_change (id, customer_order_id, position, status, changed_at)
SELECT gen_random_uuid()::text, id, 1, status, updated_at FROM customer_order WHERE status <> 'AWAITING_PAYMENT';
