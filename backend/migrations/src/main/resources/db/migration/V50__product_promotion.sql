-- The promotion of a product: the percentage off its price, from a moment (included) to a later one
-- (excluded). The three columns are all filled when the product has a promotion, all empty when it has none.
-- The products already in the catalog are not on promotion
ALTER TABLE product ADD COLUMN promotion_percentage NUMERIC(5, 2);
ALTER TABLE product ADD COLUMN promotion_starts_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE product ADD COLUMN promotion_ends_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE product ADD CONSTRAINT product_promotion_check CHECK (
    (promotion_percentage IS NULL AND promotion_starts_at IS NULL AND promotion_ends_at IS NULL)
    OR (promotion_percentage > 0 AND promotion_percentage <= 100
        AND promotion_starts_at IS NOT NULL AND promotion_ends_at > promotion_starts_at)
);
