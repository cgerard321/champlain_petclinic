USE
vets-db;

CREATE TABLE IF NOT EXISTS vets
(
    id INT
(
    4
) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vet_id INTEGER
(
    6
),
    first_name VARCHAR
(
    30
),
    last_name VARCHAR
(
    30
),
    email VARCHAR
(
    100
),
    phone_number VARCHAR
(
    30
),
    image LONGBLOB,
    resume VARCHAR
(
    350
),
    workday VARCHAR
(
    250
),
    is_active BIT,
    INDEX
(
    last_name
)
    ) engine=InnoDB;

CREATE TABLE IF NOT EXISTS specialties
(
    id INT
(
    4
) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    specialty_id INT
(
    6
),
    name VARCHAR
(
    80
),
    INDEX
(
    name
)
    ) engine=InnoDB;

CREATE TABLE IF NOT EXISTS vet_specialties
(
    vet_id INT
(
    4
) UNSIGNED NOT NULL,
    specialty_id INT
(
    4
) UNSIGNED NOT NULL,
    FOREIGN KEY
(
    vet_id
) REFERENCES vets
(
    id
),
    FOREIGN KEY
(
    specialty_id
) REFERENCES specialties
(
    id
),
    UNIQUE
(
    vet_id,
    specialty_id
)
    ) engine=InnoDB;
