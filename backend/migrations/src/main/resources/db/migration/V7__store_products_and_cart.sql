-- Products and cart were kept in memory: they are now stored in the database.
-- The tables of V1 were never used by the application (UUID ids, no category nor currency):
-- they are replaced. Orders will get their own tables when they are implemented.
DROP TABLE IF EXISTS order_line;
DROP TABLE IF EXISTS "order";
DROP TABLE IF EXISTS stock_item;
DROP TABLE IF EXISTS product;

CREATE TABLE product (
    id VARCHAR(50) PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(30) NOT NULL,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- The catalog, with the ids the shop used so far (product links keep working)
INSERT INTO product (id, name, description, category, price, currency) VALUES
    ('1', 'Organic Cotton T-shirt', 'Comfortable and eco-friendly', 'CLOTHING', 25, 'EUR'),
    ('2', 'Waterproof Jacket', 'Resists rain and wind', 'CLOTHING', 79, 'EUR'),
    ('3', 'Slim Jeans', 'Modern fit, stretch', 'CLOTHING', 49, 'EUR'),
    ('4', '5G Smartphone', 'OLED display, 128 GB', 'ELECTRONICS', 399, 'EUR'),
    ('5', '10" Tablet', 'Ideal for multimedia', 'ELECTRONICS', 249, 'EUR'),
    ('6', 'Laptop', '512 GB SSD, 16 GB RAM', 'ELECTRONICS', 799, 'EUR'),
    ('7', 'Smartwatch', 'Activity tracking and notifications', 'ACCESSORIES', 99, 'EUR'),
    ('8', 'Bluetooth Headphones', 'High-quality sound, 20h battery life', 'ACCESSORIES', 59, 'EUR'),
    ('9', 'Urban Backpack', 'Laptop compartment, modern design', 'ACCESSORIES', 39, 'EUR');

-- One line per product: adding a product already in the cart changes its quantity
CREATE TABLE cart_item (
    id VARCHAR(50) PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL UNIQUE REFERENCES product(id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
