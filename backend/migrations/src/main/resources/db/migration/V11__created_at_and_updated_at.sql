-- Every table of the JPA entities has a creation date and a last update date (set by Hibernate)

-- Creation date of the tables that did not have one: the order date for the orders and their lines,
-- the migration date otherwise
ALTER TABLE "user" ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE shop_settings ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE customer_order ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE customer_order_line ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();

-- In the time zone of the session, like the other dates written by the application
UPDATE customer_order SET created_at = placed_at::timestamp;
UPDATE customer_order_line line SET created_at = customer_order.created_at
FROM customer_order WHERE customer_order.id = line.order_id;

-- Last update date: the creation date until the next update
ALTER TABLE product ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE cart ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE cart_item ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE "user" ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE shop_settings ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE customer_order ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE customer_order_line ADD COLUMN updated_at TIMESTAMP;

UPDATE product SET updated_at = created_at;
UPDATE cart SET updated_at = created_at;
UPDATE cart_item SET updated_at = created_at;
UPDATE "user" SET updated_at = created_at;
UPDATE shop_settings SET updated_at = created_at;
UPDATE customer_order SET updated_at = created_at;
UPDATE customer_order_line SET updated_at = created_at;

ALTER TABLE product ALTER COLUMN updated_at SET NOT NULL, ALTER COLUMN updated_at SET DEFAULT now();
ALTER TABLE cart ALTER COLUMN updated_at SET NOT NULL, ALTER COLUMN updated_at SET DEFAULT now();
ALTER TABLE cart_item ALTER COLUMN updated_at SET NOT NULL, ALTER COLUMN updated_at SET DEFAULT now();
ALTER TABLE "user" ALTER COLUMN updated_at SET NOT NULL, ALTER COLUMN updated_at SET DEFAULT now();
ALTER TABLE shop_settings ALTER COLUMN updated_at SET NOT NULL, ALTER COLUMN updated_at SET DEFAULT now();
ALTER TABLE customer_order ALTER COLUMN updated_at SET NOT NULL, ALTER COLUMN updated_at SET DEFAULT now();
ALTER TABLE customer_order_line ALTER COLUMN updated_at SET NOT NULL, ALTER COLUMN updated_at SET DEFAULT now();
