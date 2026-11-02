-- The credit notes: one per refunded order, which cancels its invoice, numbered in their own sequence without
-- gaps: AV, the year of issue, then the rank of the credit note in that year (AV-2026-000007)
CREATE TABLE credit_note (
    id VARCHAR(50) PRIMARY KEY,
    number VARCHAR(20) NOT NULL UNIQUE,
    invoice_id VARCHAR(50) NOT NULL UNIQUE REFERENCES invoice(id),
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- The last rank given to a credit note, per year, incremented in the transaction that issues it (as
-- invoice_counter)
CREATE TABLE credit_note_counter (
    id VARCHAR(50) PRIMARY KEY,
    year INT NOT NULL UNIQUE,
    last_rank INT NOT NULL CHECK (last_rank >= 1),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- The orders already refunded get the credit note of their invoice, in the order they were refunded (their last
-- update when the history of their statuses does not tell it), numbered from 1 in each year
INSERT INTO credit_note (id, number, invoice_id, issued_at)
SELECT gen_random_uuid()::text,
       'AV-' || refunded.year || '-'
           || lpad((row_number() OVER (PARTITION BY refunded.year ORDER BY refunded.issued_at))::text, 6, '0'),
       refunded.invoice_id,
       refunded.issued_at
FROM (
    SELECT i.id AS invoice_id, issue.issued_at,
           extract(YEAR FROM issue.issued_at AT TIME ZONE 'Europe/Paris')::int AS year
    FROM customer_order o
    JOIN invoice i ON i.customer_order_id = o.id
    CROSS JOIN LATERAL (
        SELECT coalesce(
            (SELECT c.changed_at FROM customer_order_status_change c
             WHERE c.customer_order_id = o.id AND c.status = 'REFUNDED' ORDER BY c.position LIMIT 1),
            o.updated_at
        ) AS issued_at
    ) AS issue
    WHERE o.status = 'REFUNDED'
) AS refunded;

INSERT INTO credit_note_counter (id, year, last_rank)
SELECT gen_random_uuid()::text, split_part(number, '-', 2)::int, max(split_part(number, '-', 3)::int)
FROM credit_note
GROUP BY split_part(number, '-', 2);
