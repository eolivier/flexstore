CREATE TABLE IF NOT EXISTS "user" (
    id VARCHAR(50) PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE
);

INSERT INTO "user" (id, name, email)
VALUES
  ('u-1', 'Alice Smith', 'alice.smith@example.com'),
  ('u-2', 'Bob Johnson', 'bob.johnson@example.com'),
  ('u-3', 'Charlie Brown', 'charlie.brown@example.com')
ON CONFLICT (id) DO NOTHING;


