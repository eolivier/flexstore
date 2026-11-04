-- The products a customer saved to their wishlist, to buy them back later: at most once per customer and
-- product. Deleting the product or the customer deletes the wishlist items
CREATE TABLE wishlist_item (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    product_id VARCHAR(50) NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, product_id)
);

CREATE INDEX wishlist_item_user_id_idx ON wishlist_item(user_id);
