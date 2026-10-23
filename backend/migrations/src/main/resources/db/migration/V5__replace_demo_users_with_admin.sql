-- The demo users of V3/V4 have plain-text passwords, while sign-in compares bcrypt hashes:
-- nobody can sign in with them, so they are removed.
DELETE FROM "user" WHERE id IN ('u-1', 'u-2', 'u-3');

-- Administration account: admin@flexstore.com / admin (password stored as a bcrypt hash)
INSERT INTO "user" (id, name, email, password)
VALUES ('u-admin', 'Admin', 'admin@flexstore.com', '$2a$10$dYQWMZYwfKo5WLg4JuyMvOFM7rxJ16w3Y/L4FcZaUcNmrq10KIRRO')
ON CONFLICT (email) DO NOTHING;
