-- The number of units at or under which the administrators are warned that a product runs low, given by them
-- for each product. The stocks already given take the default threshold, 5 units
ALTER TABLE stock ADD COLUMN alert_threshold INT NOT NULL DEFAULT 5 CHECK (alert_threshold >= 0);
