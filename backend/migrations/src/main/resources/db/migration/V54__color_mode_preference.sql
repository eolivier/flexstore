-- The display mode (light or dark) a signed-in customer chose, kept in their account so that every browser
-- they sign in from shows the shop the way they like: at most one per customer, none until they choose
CREATE TABLE color_mode_preference (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    color_mode VARCHAR(20) NOT NULL CHECK (color_mode IN ('LIGHT', 'DARK')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
