-- The settings of a signed-in customer, kept in their account: one row per setting the customer chose, at most
-- one per customer and setting name, replaced by each new choice. A new setting is a new authorized name and its
-- authorized values in the checks below, not a new column. It replaces cookie_consent (V42) and user_preference
-- (V54, V56), which were two tables of the same kind (one row per customer):
-- - color_mode: the display mode, LIGHT or DARK; a customer who never chose has no row and their browser
--   decides;
-- - language: the preferred language, EN or FR; a customer who never chose has no row and the page decides;
-- - cookie_choice: the choice about the preference cookies, ACCEPTED or REFUSED, with the moment it expires
--   (expires_at: the customer is asked again); a customer who was never asked has no row
CREATE TABLE user_setting (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    name VARCHAR(30) NOT NULL CHECK (name IN ('color_mode', 'language', 'cookie_choice')),
    value VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, name),
    CHECK (
        (name = 'color_mode' AND value IN ('LIGHT', 'DARK'))
        OR (name = 'language' AND value IN ('EN', 'FR'))
        OR (name = 'cookie_choice' AND value IN ('ACCEPTED', 'REFUSED'))
    ),
    CHECK ((name = 'cookie_choice') = (expires_at IS NOT NULL))
);

-- Each preference filled in a row of user_preference becomes a setting, the row of a customer who chose both
-- giving two settings: the display mode keeps the id of the row, the language gets a new one
INSERT INTO user_setting (id, user_id, name, value, created_at, updated_at)
SELECT id, user_id, 'color_mode', color_mode, created_at, updated_at
FROM user_preference
WHERE color_mode IS NOT NULL;

INSERT INTO user_setting (id, user_id, name, value, created_at, updated_at)
SELECT CASE WHEN color_mode IS NULL THEN id ELSE gen_random_uuid()::text END,
       user_id, 'language', language, created_at, updated_at
FROM user_preference
WHERE language IS NOT NULL;

INSERT INTO user_setting (id, user_id, name, value, expires_at, created_at, updated_at)
SELECT id, user_id, 'cookie_choice', choice, expires_at, created_at, updated_at
FROM cookie_consent;

DROP TABLE cookie_consent;
DROP TABLE user_preference;
