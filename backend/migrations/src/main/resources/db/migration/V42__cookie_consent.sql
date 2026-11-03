-- The choice of a signed-in customer about the preference cookies (the essential ones need no consent): at most
-- one per customer, valid until expires_at (6 months after the choice), after which the customer is asked again
CREATE TABLE cookie_consent (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    choice VARCHAR(20) NOT NULL CHECK (choice IN ('ACCEPTED', 'REFUSED')),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
