DROP TABLE IF EXISTS billings;

CREATE TABLE billings (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    bill_id INT,
    customer_id VARCHAR(80),
    visit_type VARCHAR(80),
    visit_date DATE,
    amount DECIMAL(10,2),
    taxed_amount DECIMAL(10,2),
    interest DECIMAL(10,2)
);