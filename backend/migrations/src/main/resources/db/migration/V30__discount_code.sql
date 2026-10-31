-- The discount codes given by the administrators: a percentage (1 to 100) or an amount (in euros) off the price
-- of the items of an order, until a date, for a number of orders
CREATE TABLE discount_code (
    id VARCHAR(50) PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    reduction_type VARCHAR(10) NOT NULL CHECK (reduction_type IN ('PERCENTAGE', 'AMOUNT')),
    reduction_value NUMERIC(12, 2) NOT NULL CHECK (reduction_value > 0),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    usage_limit INT NOT NULL CHECK (usage_limit >= 1),
    times_used INT NOT NULL DEFAULT 0 CHECK (times_used >= 0 AND times_used <= usage_limit),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- The discount of an order: a copy of the code entered and of the amount it took off, without foreign key (the
-- order keeps its discount when the code is deleted). No code: no discount, and an amount of 0
ALTER TABLE customer_order ADD COLUMN discount_code VARCHAR(30);
ALTER TABLE customer_order ADD COLUMN discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0
    CHECK (discount_amount >= 0);
