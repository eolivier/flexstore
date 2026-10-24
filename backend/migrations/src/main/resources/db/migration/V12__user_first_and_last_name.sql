-- A user has a first name and a last name instead of a single name.
-- The existing names are split: the first word is the first name, the rest is the last name
-- (empty for a one-word name, like the administration account "Admin")
ALTER TABLE "user" ADD COLUMN first_name VARCHAR(255);
ALTER TABLE "user" ADD COLUMN last_name VARCHAR(255);

UPDATE "user" SET
    first_name = split_part(btrim(name), ' ', 1),
    last_name = CASE
        WHEN position(' ' IN btrim(name)) > 0 THEN btrim(substring(btrim(name) FROM position(' ' IN btrim(name)) + 1))
        ELSE ''
    END;

ALTER TABLE "user" ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE "user" ALTER COLUMN last_name SET NOT NULL;
ALTER TABLE "user" DROP COLUMN name;
