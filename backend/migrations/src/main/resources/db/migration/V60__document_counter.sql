-- The last rank given to a document of a kind (INVOICE or CREDIT_NOTE), per year. It replaces invoice_counter
-- (V29) and credit_note_counter (V39), which were two tables of the same kind. It is incremented in the
-- transaction that issues the document (a PostgreSQL sequence is not transactional: an issue rolled back would
-- leave a gap in the numbering)
CREATE TABLE document_counter (
    id VARCHAR(50) PRIMARY KEY,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('INVOICE', 'CREDIT_NOTE')),
    year INT NOT NULL,
    last_rank INT NOT NULL CHECK (last_rank >= 1),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (kind, year)
);

INSERT INTO document_counter (id, kind, year, last_rank, created_at, updated_at)
SELECT id, 'INVOICE', year, last_rank, created_at, updated_at
FROM invoice_counter;

INSERT INTO document_counter (id, kind, year, last_rank, created_at, updated_at)
SELECT id, 'CREDIT_NOTE', year, last_rank, created_at, updated_at
FROM credit_note_counter;

DROP TABLE invoice_counter;
DROP TABLE credit_note_counter;
