-- How the seller is incorporated, written on the invoices: its legal form, its share capital in euros and the
-- city of its trade and companies register (RCS). The administrators give them with each new version of the
-- seller. The versions written before have none (NULL): an invoice keeps the seller it was issued with, so the
-- invoices of those versions show none
ALTER TABLE seller ADD COLUMN legal_form VARCHAR(50);
ALTER TABLE seller ADD COLUMN share_capital VARCHAR(30);
ALTER TABLE seller ADD COLUMN registry_city VARCHAR(100);

-- The demonstration seller of V31 (Flexstore SAS, its demonstration SIRET), while it is still the current one:
-- a new version with its incorporation, so that the next invoices of the demonstration show it. A seller the
-- administrators wrote is never completed with invented values
INSERT INTO seller (id, version, name, address, siret, vat_number, phone, email, website, legal_form,
                    share_capital, registry_city)
SELECT gen_random_uuid()::text, version + 1, name, address, siret, vat_number, phone, email, website, 'SAS',
       '10 000', 'Paris'
FROM (SELECT * FROM seller ORDER BY version DESC LIMIT 1) AS current_seller
WHERE name = 'Flexstore SAS' AND siret = '123 456 789 00012';
