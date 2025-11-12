CREATE TABLE IF NOT EXISTS product (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT,
  price_cents BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS stock_item (
  product_id UUID PRIMARY KEY REFERENCES product(id) ON DELETE CASCADE,
  quantity INT NOT NULL CHECK (quantity >= 0),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS "order" (
  id UUID PRIMARY KEY,
  total_cents BIGINT NOT NULL,
  status TEXT NOT NULL, -- e.g. CREATED, PAID, CANCELLED
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS order_line (
  order_id UUID REFERENCES "order"(id) ON DELETE CASCADE,
  product_id UUID REFERENCES product(id),
  qty INT NOT NULL CHECK (qty > 0),
  unit_price_cents BIGINT NOT NULL,
  PRIMARY KEY (order_id, product_id)
);
