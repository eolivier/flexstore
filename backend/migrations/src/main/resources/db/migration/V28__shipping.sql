-- How an order is shipped (STANDARD or EXPRESS) and what its shipping cost, fixed when it is placed. The orders
-- placed before had no shipping: shipped the standard way, for free
ALTER TABLE customer_order ADD COLUMN shipping_method VARCHAR(20);
ALTER TABLE customer_order ADD COLUMN shipping_cost NUMERIC(12, 2);
UPDATE customer_order SET shipping_method = 'STANDARD', shipping_cost = 0;
ALTER TABLE customer_order ALTER COLUMN shipping_method SET NOT NULL;
ALTER TABLE customer_order ALTER COLUMN shipping_cost SET NOT NULL;
ALTER TABLE customer_order ADD CONSTRAINT customer_order_shipping_cost_check CHECK (shipping_cost >= 0);

-- The price of the items of an order from which its standard shipping is free, in euros (0: always free)
ALTER TABLE shop_settings ADD COLUMN free_shipping_threshold NUMERIC(12, 2) NOT NULL DEFAULT 50
    CHECK (free_shipping_threshold >= 0);
