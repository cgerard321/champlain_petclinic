DROP TABLE billings IF EXISTS;

CREATE TABLE billings (
    id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    billId Int,
    customerId INT,
    visitType VARCHAR(80),
    date DATE,
    amount DOUBLE
);