-- The version of the terms of sale the customer accepted to place an order (the versions of a legal page are
-- kept, never deleted). The orders placed before have none: their customers did not accept terms of sale
ALTER TABLE customer_order ADD COLUMN legal_page_id VARCHAR(50) REFERENCES legal_page(id);
