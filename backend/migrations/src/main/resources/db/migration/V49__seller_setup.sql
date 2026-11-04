-- Whether the seller of the shop was given through the setup assistant, shown to the administrators but not
-- changed by them: it is turned on once the assistant is finished. Until then, the assistant is offered
ALTER TABLE shop_settings ADD COLUMN seller_setup VARCHAR(10) NOT NULL DEFAULT 'DISABLED'
    CHECK (seller_setup IN ('ENABLED', 'DISABLED'));

-- A shop whose seller was already edited by an administrator (a second version exists) has been set up: only the
-- first version, given by the configuration, is left to be completed by the assistant
UPDATE shop_settings SET seller_setup = 'ENABLED' WHERE (SELECT COUNT(*) FROM seller) > 1;
