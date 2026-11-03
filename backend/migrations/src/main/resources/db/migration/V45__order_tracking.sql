-- The carrier and the tracking number of the parcel of an order, given by an administrator when the order is
-- shipped, or corrected afterwards. Both are given together, or none: the orders already shipped have none
ALTER TABLE customer_order ADD COLUMN tracking_carrier VARCHAR(20);
ALTER TABLE customer_order ADD COLUMN tracking_number VARCHAR(40);
ALTER TABLE customer_order ADD CONSTRAINT customer_order_tracking_given_together
    CHECK ((tracking_carrier IS NULL) = (tracking_number IS NULL));
