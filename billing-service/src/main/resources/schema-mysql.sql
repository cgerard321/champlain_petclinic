USE `billing-db`;
DROP TABLE IF EXISTS billings;
CREATE TABLE IF NOT EXISTS billings (
    id INT(7) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    bill_id Int,
    customer_id INT,
    visit_type VARCHAR(80),
    visit_date DATE,
    amount DOUBLE
    );-- engine=InnoDB;