-- The payments started for the orders on the payment provider (Stripe, or a simulated one).
-- An order refers to its last payment: the customer may start another one after leaving the checkout page
CREATE TABLE payment (
    id VARCHAR(50) PRIMARY KEY,
    -- The id of the session on the provider (cs_... for Stripe)
    provider_reference VARCHAR(255) NOT NULL UNIQUE,
    checkout_url TEXT NOT NULL,
    outcome VARCHAR(10) NOT NULL CHECK (outcome IN ('PENDING', 'PAID')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- The orders placed before were not paid online, and an order awaiting its payment may not have started one
ALTER TABLE customer_order ADD COLUMN payment_id VARCHAR(50) REFERENCES payment(id);
