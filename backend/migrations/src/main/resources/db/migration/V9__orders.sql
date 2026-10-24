-- Orders placed by the customers from their cart ("order" is a reserved word: customer_order)
CREATE TABLE customer_order (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    placed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX customer_order_user_id_idx ON customer_order(user_id);

-- A line keeps the name and the price of the product when the order was placed:
-- no foreign key to product, the order history does not depend on the catalog
CREATE TABLE customer_order_line (
    id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL REFERENCES customer_order(id) ON DELETE CASCADE,
    position INT NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    product_name TEXT NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    currency VARCHAR(3) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    UNIQUE (order_id, position)
);
