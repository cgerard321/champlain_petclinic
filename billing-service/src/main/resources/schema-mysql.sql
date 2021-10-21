USE `billing-db`;

CREATE TABLE IF NOT EXISTS billings (
    id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    bill_id Int,
    customer_id INT,
    visit_type VARCHAR(80),
    visit_date DATE,
    amount DOUBLE
    ) engine=InnoDB;