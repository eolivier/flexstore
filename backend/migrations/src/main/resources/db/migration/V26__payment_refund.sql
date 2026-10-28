-- The refund of a payment, when an administrator cancels a paid order before it is shipped: at most one per
-- payment, with its reference on the payment provider (the id of the Stripe refund)
CREATE TABLE payment_refund (
    id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL UNIQUE REFERENCES payment(id) ON DELETE CASCADE,
    provider_reference VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
