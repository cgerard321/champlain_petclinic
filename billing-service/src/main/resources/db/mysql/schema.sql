CREATE DATABASE IF NOT EXISTS petclinic;
GRANT ALL PRIVILEGES ON petclinic.* TO user@localhost IDENTIFIED BY 'pwd';

USE petclinic;

CREATE TABLE IF NOT EXISTS billings (
    id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment_method VARCHAR(30),
    bill_date  TIMESTAMP ,
    invoice_date TIMESTAMP,
    customer_id int,
    purchase_type VARCHAR(100),
    bill_total VARCHAR(30)
    FOREIGN KEY (customer_id) REFERENCES owners(id),
    INDEX(payment_method)
    ) engine=InnoDB;