-- Role used for authorization: ADMIN can manage the users and the products
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS role TEXT NOT NULL DEFAULT 'USER';

UPDATE "user" SET role = 'ADMIN' WHERE email = 'admin@flexstore.com';
