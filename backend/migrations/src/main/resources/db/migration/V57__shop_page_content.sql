-- The content of the shop pages, edited by the administrators: one row for each type (the key words and the
-- description of the shop in its head, the Google Analytics identifier). A new type is a new row, not a column
CREATE TABLE shop_page_content (
    id VARCHAR(50) PRIMARY KEY,
    page_type VARCHAR(50) NOT NULL UNIQUE
        CHECK (page_type IN ('META_KEY_WORDS', 'GOOGLE_ANALYTICS_ID', 'META_SHOP_DESCRIPTION')),
    content TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Every type has its row from the start, empty until the administrators write it
INSERT INTO shop_page_content (id, page_type) VALUES
    (gen_random_uuid()::text, 'META_KEY_WORDS'),
    (gen_random_uuid()::text, 'GOOGLE_ANALYTICS_ID'),
    (gen_random_uuid()::text, 'META_SHOP_DESCRIPTION');
