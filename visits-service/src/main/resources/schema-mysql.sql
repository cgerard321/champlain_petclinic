USE `visits-db`;

CREATE TABLE IF NOT EXISTS visits (
    id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    pet_id INT(4) UNSIGNED NOT NULL,
    visit_date DATE,
    description VARCHAR(8192),
    practitioner_id INT(4) UNSIGNED NOT NULL,
    ) engine=InnoDB;
