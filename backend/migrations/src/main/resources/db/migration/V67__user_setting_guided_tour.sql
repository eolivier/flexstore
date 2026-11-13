-- The guided tour of the shop joins the settings of a signed-in customer: guided_tour, SHOWN or HIDDEN. A
-- customer who never chose has no row and the tour is shown to them. The checks of V58 are dropped (whatever
-- their generated names) and written again, named, with the new setting
DO $$
DECLARE
    check_constraint RECORD;
BEGIN
    FOR check_constraint IN
        SELECT conname FROM pg_constraint WHERE conrelid = 'user_setting'::regclass AND contype = 'c'
    LOOP
        EXECUTE format('ALTER TABLE user_setting DROP CONSTRAINT %I', check_constraint.conname);
    END LOOP;
END $$;

ALTER TABLE user_setting ADD CONSTRAINT user_setting_name_check
    CHECK (name IN ('color_mode', 'language', 'cookie_choice', 'guided_tour'));

ALTER TABLE user_setting ADD CONSTRAINT user_setting_value_check
    CHECK (
        (name = 'color_mode' AND value IN ('LIGHT', 'DARK'))
        OR (name = 'language' AND value IN ('EN', 'FR'))
        OR (name = 'cookie_choice' AND value IN ('ACCEPTED', 'REFUSED'))
        OR (name = 'guided_tour' AND value IN ('SHOWN', 'HIDDEN'))
    );

ALTER TABLE user_setting ADD CONSTRAINT user_setting_expires_at_check
    CHECK ((name = 'cookie_choice') = (expires_at IS NOT NULL));
