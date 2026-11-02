-- How the customers reach the seller, written on the invoices: its phone number, its email and its website. The
-- administrators give them with each new version of the seller. The versions written before have none (NULL):
-- they cannot be filled without inventing them, and an invoice keeps the seller it was issued with, so the
-- invoices of those versions show none
ALTER TABLE seller ADD COLUMN phone VARCHAR(30);
ALTER TABLE seller ADD COLUMN email VARCHAR(255);
ALTER TABLE seller ADD COLUMN website VARCHAR(255);
