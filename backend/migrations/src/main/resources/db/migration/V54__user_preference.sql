-- The preferences of a signed-in customer, kept in their account so that every browser they sign in from shows
-- the shop the way they like: at most one row per customer, created when they first choose one (the display
-- mode, light or dark, for now); a customer without a row has no preference and their browser decides
CREATE TABLE user_preference (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    color_mode VARCHAR(20) NOT NULL CHECK (color_mode IN ('LIGHT', 'DARK')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
