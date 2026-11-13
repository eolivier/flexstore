-- The refund of a payment is a column of the payment, as a payment has at most one refund (the table
-- payment_refund, V26, had at most one row per payment): the reference of the refund on the payment provider
-- (the id of the Stripe refund), empty while the payment is not refunded
ALTER TABLE payment ADD COLUMN refund_provider_reference VARCHAR(255) UNIQUE;

UPDATE payment
SET refund_provider_reference = payment_refund.provider_reference
FROM payment_refund
WHERE payment_refund.payment_id = payment.id;

DROP TABLE payment_refund;
