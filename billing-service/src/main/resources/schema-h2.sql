DROP TABLE billings IF EXISTS;

CREATE TABLE billings
(
    id INTEGER IDENTITY PRIMARY KEY,
    payment_method VARCHAR(30),
    bill_date  TIMESTAMP,
    invoice_date TIMESTAMP,
    customer_id int,
    purchase_type VARCHAR(100),
    bill_total VARCHAR(30)
);
CREATE INDEX payment_method ON billings (payment_method);

