-- The name and the description of a product are only kept in product_translation, in every language, the
-- default one (en) included: the name and the description of the table product become its row in en
INSERT INTO product_translation (id, product_id, language, name, description, created_at, updated_at)
SELECT gen_random_uuid()::text, id, 'en', name, description, created_at, updated_at
FROM product
ON CONFLICT (product_id, language) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description;

ALTER TABLE product DROP COLUMN name, DROP COLUMN description;
