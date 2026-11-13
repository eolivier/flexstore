-- The credit note of a refunded order is two columns of the invoice it cancels, as an invoice has at most one
-- credit note (the table credit_note, V39, had at most one row per invoice): its number (AV, the year of issue,
-- then its rank in the year: AV-2026-000007) and the moment it was issued. Both are empty while the invoice is
-- not cancelled. The credit notes keep their own sequence of numbers (document_counter)
ALTER TABLE invoice ADD COLUMN credit_note_number VARCHAR(20) UNIQUE;
ALTER TABLE invoice ADD COLUMN credit_note_issued_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE invoice ADD CHECK ((credit_note_number IS NULL) = (credit_note_issued_at IS NULL));

UPDATE invoice
SET credit_note_number = credit_note.number,
    credit_note_issued_at = credit_note.issued_at
FROM credit_note
WHERE credit_note.invoice_id = invoice.id;

DROP TABLE credit_note;
