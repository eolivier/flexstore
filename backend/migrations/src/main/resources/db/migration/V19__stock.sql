-- The number of units that the shop has of each product: an order is refused when it asks for more,
-- a paid order takes its units out. A product without a row has none
CREATE TABLE stock (
    id VARCHAR(50) PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL UNIQUE REFERENCES product(id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- The products of the catalog stay orderable: 100 units each, to be adjusted by the administrators
INSERT INTO stock (id, product_id, quantity)
SELECT gen_random_uuid()::text, id, 100 FROM product;
