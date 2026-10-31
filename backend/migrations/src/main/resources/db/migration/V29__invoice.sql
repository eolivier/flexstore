-- The invoices: one per paid order, numbered in a sequence without gaps: the year of issue, then the rank of the
-- invoice in that year (2026-000042)
CREATE TABLE invoice (
    id VARCHAR(50) PRIMARY KEY,
    number VARCHAR(20) NOT NULL UNIQUE,
    customer_order_id VARCHAR(50) NOT NULL UNIQUE REFERENCES customer_order(id),
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- The last rank given to an invoice, per year. It is incremented in the transaction that issues the invoice (a
-- PostgreSQL sequence is not transactional: an issue rolled back would leave a gap in the numbering)
CREATE TABLE invoice_counter (
    id VARCHAR(50) PRIMARY KEY,
    year INT NOT NULL UNIQUE,
    last_rank INT NOT NULL CHECK (last_rank >= 1),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- The orders already paid are invoiced in the order they were paid (their last update when the history of their
-- statuses does not tell it), numbered from 1 in each year
INSERT INTO invoice (id, number, customer_order_id, issued_at)
SELECT gen_random_uuid()::text,
       paid.year || '-'
           || lpad((row_number() OVER (PARTITION BY paid.year ORDER BY paid.issued_at))::text, 6, '0'),
       paid.id,
       paid.issued_at
FROM (
    SELECT o.id, issue.issued_at, extract(YEAR FROM issue.issued_at AT TIME ZONE 'Europe/Paris')::int AS year
    FROM customer_order o
    CROSS JOIN LATERAL (
        SELECT coalesce(
            (SELECT c.changed_at FROM customer_order_status_change c
             WHERE c.customer_order_id = o.id AND c.status = 'PAID' ORDER BY c.position LIMIT 1),
            o.updated_at
        ) AS issued_at
    ) AS issue
    WHERE o.status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'REFUNDED')
) AS paid;

INSERT INTO invoice_counter (id, year, last_rank)
SELECT gen_random_uuid()::text, split_part(number, '-', 1)::int, max(split_part(number, '-', 2)::int)
FROM invoice
GROUP BY split_part(number, '-', 1);
