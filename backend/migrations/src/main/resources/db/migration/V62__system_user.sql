-- The system user runs the scheduled use cases (cancelling the unpaid orders, reminding the abandoned
-- carts). Its
-- role is SYSTEM. Nobody signs in with it: the empty password matches no hash, and the sign-in refuses its role
INSERT INTO "user" (id, first_name, last_name, email, password, role, status)
VALUES ('system', 'System', 'System', 'system@flexstore.local', '', 'SYSTEM', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
