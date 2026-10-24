-- The settings of the shop, chosen by the administrators: a single row
CREATE TABLE shop_settings (
    id INT PRIMARY KEY CHECK (id = 1),
    order_confirmation_email VARCHAR(10) NOT NULL CHECK (order_confirmation_email IN ('ENABLED', 'DISABLED'))
);

INSERT INTO shop_settings (id, order_confirmation_email) VALUES (1, 'ENABLED');
