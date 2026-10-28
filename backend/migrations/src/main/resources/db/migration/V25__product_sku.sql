-- The SKU (Stock Keeping Unit) of a product: the reference the shop gives to it, unique in the catalog
ALTER TABLE product ADD COLUMN sku VARCHAR(32);

-- The products already in the catalog get the first three letters of their category, then the beginning of their
-- id without hyphens (CLO-1 for the demo T-shirt): the administrators can change it afterwards
UPDATE product SET sku = upper(left(category, 3)) || '-' || upper(left(replace(id, '-', ''), 12));

ALTER TABLE product ALTER COLUMN sku SET NOT NULL;
ALTER TABLE product ADD CONSTRAINT product_sku_key UNIQUE (sku);
