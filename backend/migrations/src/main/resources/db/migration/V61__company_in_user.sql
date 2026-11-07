-- The company a customer buys for is three columns of the user, as a user has at most one company (the table
-- customer_company, V34, had one row per user): its name and its SIREN (9 digits), filled together, and its
-- intra-community VAT number when it gives one. All empty for a customer who buys for themselves
ALTER TABLE "user" ADD COLUMN company_name VARCHAR(255);
ALTER TABLE "user" ADD COLUMN company_siren VARCHAR(9) CHECK (company_siren ~ '^[0-9]{9}$');
ALTER TABLE "user" ADD COLUMN company_vat_number VARCHAR(30);
ALTER TABLE "user" ADD CHECK ((company_name IS NULL) = (company_siren IS NULL));
ALTER TABLE "user" ADD CHECK (company_vat_number IS NULL OR company_name IS NOT NULL);

UPDATE "user"
SET company_name = company.company_name,
    company_siren = company.siren,
    company_vat_number = company.vat_number
FROM customer_company company
WHERE company.user_id = "user".id;

DROP TABLE customer_company;
