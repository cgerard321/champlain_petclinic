CREATE DATABASE IF NOT EXISTS billing;
GRANT ALL PRIVILEGES ON petclinic.* TO user@localhost IDENTIFIED BY 'pwd';

USE petclinic;

CREATE TABLE IF NOT EXISTS billings (
    id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customerId INT,
    visitType VARCHAR(80),
    date DATE,
    amount DOUBLE
    ) engine=InnoDB;