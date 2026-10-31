-- The seller written on the invoices, edited by the administrators. Each edit adds a version, never changes one:
-- an invoice references the version current when it was issued, so that it keeps the seller it was issued
-- with. The current seller is the version with the highest number
CREATE TABLE seller (
    id VARCHAR(50) PRIMARY KEY,
    version INT NOT NULL UNIQUE CHECK (version >= 1),
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    siret VARCHAR(20) NOT NULL,
    vat_number VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- The first version: the seller given until now by the configuration (flexstore.seller), its default values
INSERT INTO seller (id, version, name, address, siret, vat_number)
VALUES (gen_random_uuid()::text, 1, 'Flexstore SAS', '1 rue de la Démo, 75001 Paris, France',
        '123 456 789 00012', 'FR12345678901');

-- The invoices already issued were written with that first version
ALTER TABLE invoice ADD COLUMN seller_id VARCHAR(50) REFERENCES seller(id);
UPDATE invoice SET seller_id = (SELECT id FROM seller WHERE version = 1);
ALTER TABLE invoice ALTER COLUMN seller_id SET NOT NULL;
