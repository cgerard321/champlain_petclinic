USE `billing-db`;

DROP TABLE IF EXISTS billings;

CREATE TABLE  IF NOT EXISTS billings (
   id INT(7) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
   bill_id INT,
   customer_id VARCHAR(80),
   visit_type VARCHAR(80),
   visit_date DATE,
   amount DECIMAL(10,2),
   taxed_amount DECIMAL(10,2),
   interest DECIMAL(10,2),
   due_date DATE,
   bill_status VARCHAR(20)
);