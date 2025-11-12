-- Produits de démo
INSERT INTO product (id, name, description, price_cents) VALUES
  ('11111111-1111-1111-1111-111111111111','Flex Tee','T-shirt Flexstore',1999),
  ('22222222-2222-2222-2222-222222222222','Flex Hoodie','Hoodie Flexstore',4999),
  ('33333333-3333-3333-3333-333333333333','Flex Mug','Mug Flexstore',1299)
ON CONFLICT (id) DO NOTHING;

-- Stock de démo
INSERT INTO stock_item (product_id, quantity) VALUES
  ('11111111-1111-1111-1111-111111111111', 25),
  ('22222222-2222-2222-2222-222222222222', 10),
  ('33333333-3333-3333-3333-333333333333', 50)
ON CONFLICT (product_id) DO UPDATE SET quantity = EXCLUDED.quantity, updated_at = now();
