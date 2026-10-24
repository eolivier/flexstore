-- One cart per owner instead of a single cart shared by every visitor:
-- a signed-in customer (user_id) or a visitor (no user_id, the id is kept by the client).
CREATE TABLE cart (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- The items of the shared cart have no owner: they are dropped
DELETE FROM cart_item;

ALTER TABLE cart_item ADD COLUMN cart_id VARCHAR(50) NOT NULL REFERENCES cart(id) ON DELETE CASCADE;
ALTER TABLE cart_item DROP CONSTRAINT cart_item_product_id_key;
ALTER TABLE cart_item ADD CONSTRAINT cart_item_cart_id_product_id_key UNIQUE (cart_id, product_id);
