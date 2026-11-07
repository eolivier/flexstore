-- The preferred language of a signed-in customer joins their display mode in their user_preference row: a
-- customer may now have chosen only one of the two, so each column is empty until the customer chooses it
ALTER TABLE user_preference ALTER COLUMN color_mode DROP NOT NULL;
ALTER TABLE user_preference ADD COLUMN language VARCHAR(2) CHECK (language IN ('EN', 'FR'));
