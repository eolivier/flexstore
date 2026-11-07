-- The stock of a product is two columns of the product, as a product has at most one stock (the table stock,
-- V19 and V41, had one row per product):
-- - stock_quantity: the number of units, empty while the stock was never given (a product without stock has
--   none and is not warned about);
-- - stock_alert_threshold: the number of units at or under which the administrators are warned, 5 by default
ALTER TABLE product ADD COLUMN stock_quantity INT CHECK (stock_quantity >= 0);
ALTER TABLE product ADD COLUMN stock_alert_threshold INT NOT NULL DEFAULT 5 CHECK (stock_alert_threshold >= 0);

UPDATE product
SET stock_quantity = stock.quantity,
    stock_alert_threshold = stock.alert_threshold
FROM stock
WHERE stock.product_id = product.id;

DROP TABLE stock;
