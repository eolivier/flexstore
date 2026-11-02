-- The legal pages of the shop (its legal notice and its terms of sale), written by the administrators and read
-- by everyone. Each publication adds a version, never changes one, so that the terms of sale of a given date can
-- be found again. The current page of a kind is its version with the highest number
CREATE TABLE legal_page (
    id VARCHAR(50) PRIMARY KEY,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('LEGAL_NOTICE', 'TERMS_OF_SALE')),
    version INT NOT NULL CHECK (version >= 1),
    text TEXT NOT NULL CHECK (length(text) <= 50000),
    published_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (kind, version)
);

-- The first version of each page: empty, until the administrators write it
INSERT INTO legal_page (id, kind, version, text, published_at)
VALUES (gen_random_uuid()::text, 'LEGAL_NOTICE', 1, '', now()),
       (gen_random_uuid()::text, 'TERMS_OF_SALE', 1, '', now());
