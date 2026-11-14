-- The shipping methods, managed by the administrators: a name, a cost, whether the shipping is free from the
-- threshold of the shop settings (the standard one) or always at its cost (the express one), and whether the
-- method is proposed to the customers (ENABLED) or not any more (DISABLED). A method is never deleted: the
-- orders placed with it keep it
CREATE TABLE shipping_method (
    id VARCHAR(50) PRIMARY KEY,
    label VARCHAR(255) NOT NULL,
    cost NUMERIC(12, 2) NOT NULL CHECK (cost >= 0),
    free_shipping_eligibility VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- The two methods the shop had before, under the ids the orders already reference
INSERT INTO shipping_method (id, label, cost, free_shipping_eligibility, status) VALUES
    ('STANDARD', 'Livraison standard', 4.90, 'ELIGIBLE', 'ENABLED'),
    ('EXPRESS', 'Livraison express', 9.90, 'NOT_ELIGIBLE', 'ENABLED');

-- An order references the shipping method it was placed with; its shipping cost stays its own, fixed when it was
-- placed
ALTER TABLE customer_order RENAME COLUMN shipping_method TO shipping_method_id;
ALTER TABLE customer_order ADD CONSTRAINT customer_order_shipping_method_id_fkey
    FOREIGN KEY (shipping_method_id) REFERENCES shipping_method(id);
CREATE INDEX customer_order_shipping_method_id_idx ON customer_order(shipping_method_id);
