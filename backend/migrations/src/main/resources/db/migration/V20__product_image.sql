-- The image of a product, given by an administrator and kept resized in each format: LARGE for the page of the
-- product (500 KB at most), SMALL for the cards of the catalog and the cart (60 KB at most)
CREATE TABLE product_image (
    id VARCHAR(50) PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    format VARCHAR(10) NOT NULL CHECK (format IN ('LARGE', 'SMALL')),
    media_type VARCHAR(30) NOT NULL,
    content BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (product_id, format)
);
