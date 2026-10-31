-- The rate of VAT of each product, chosen by the administrators: STANDARD (20 %), INTERMEDIATE (10 %), REDUCED
-- (5.5 %) or SUPER_REDUCED (2.1 %). The products of the catalog were all sold at the standard rate until now
ALTER TABLE product ADD COLUMN vat_rate VARCHAR(20);
UPDATE product SET vat_rate = 'STANDARD';
ALTER TABLE product ALTER COLUMN vat_rate SET NOT NULL;
ALTER TABLE product ADD CONSTRAINT product_vat_rate_check
    CHECK (vat_rate IN ('STANDARD', 'INTERMEDIATE', 'REDUCED', 'SUPER_REDUCED'));

-- Copied into each line when the order is placed, like the price: the invoice of the order keeps the rate of
-- that time, whatever the rate of the product becomes. The orders placed until now were at the standard rate
ALTER TABLE customer_order_line ADD COLUMN vat_rate VARCHAR(20);
UPDATE customer_order_line SET vat_rate = 'STANDARD';
ALTER TABLE customer_order_line ALTER COLUMN vat_rate SET NOT NULL;
ALTER TABLE customer_order_line ADD CONSTRAINT customer_order_line_vat_rate_check
    CHECK (vat_rate IN ('STANDARD', 'INTERMEDIATE', 'REDUCED', 'SUPER_REDUCED'));
