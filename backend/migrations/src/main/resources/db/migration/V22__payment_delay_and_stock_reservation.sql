-- The time a customer has to pay an order once placed, chosen by the administrators: the units of the order are
-- reserved meanwhile, and an order not paid in time is cancelled (status CANCELLED), which gives them back
ALTER TABLE shop_settings ADD COLUMN payment_delay_minutes INT NOT NULL DEFAULT 10
    CHECK (payment_delay_minutes BETWEEN 1 AND 1440);

-- The orders awaiting their payment now reserve their units, as the new orders do: when they are cancelled,
-- the units they give back were taken out
UPDATE stock
SET quantity = GREATEST(0, stock.quantity - reserved.quantity)
FROM (
    SELECT line.product_id, SUM(line.quantity) AS quantity
    FROM customer_order_line line
    JOIN customer_order ON customer_order.id = line.customer_order_id
    WHERE customer_order.status = 'AWAITING_PAYMENT'
    GROUP BY line.product_id
) reserved
WHERE stock.product_id = reserved.product_id;
