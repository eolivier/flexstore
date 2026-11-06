-- The reminder sent by email to a customer who left items in their cart, chosen by the administrators: turned
-- off by default (the shop decides to write to its customers), sent once the cart has been left for the delay
ALTER TABLE shop_settings ADD COLUMN abandoned_cart_reminder VARCHAR(20) NOT NULL DEFAULT 'DISABLED'
    CHECK (abandoned_cart_reminder IN ('ENABLED', 'DISABLED'));
ALTER TABLE shop_settings ADD COLUMN abandoned_cart_delay_hours INT NOT NULL DEFAULT 24
    CHECK (abandoned_cart_delay_hours BETWEEN 1 AND 720);

-- The moment the items of a cart last changed (updated_at also moves when the reminder is marked), and the
-- moment its customer was reminded of it (empty until then, and again after each change of the cart)
ALTER TABLE cart ADD COLUMN last_changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now();
ALTER TABLE cart ADD COLUMN reminded_at TIMESTAMP WITH TIME ZONE;

-- The carts that exist before the reminder are not reminded all at once when the administrators turn it on: they
-- are reminded once their customer changes them again
UPDATE cart SET reminded_at = now();

CREATE INDEX cart_abandoned_idx ON cart(last_changed_at) WHERE user_id IS NOT NULL AND reminded_at IS NULL;
