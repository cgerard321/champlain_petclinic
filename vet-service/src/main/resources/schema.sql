CREATE TABLE if NOT EXISTS vet
(
    id INT NOT NULL AUTO_INCREMENT,
    vet_id       INT NOT NULL UNIQUE,
    first_name   VARCHAR(30),
    last_name    VARCHAR(30),
    email        VARCHAR(100),
    phone_number VARCHAR(30),
    image        LONGBLOB,
    resume       VARCHAR(350),
    workday      VARCHAR(250),
    is_active    TINYINT,
    primary key(id)
);

CREATE TABLE if NOT EXISTS specialties
(
    id INT NOT NULL AUTO_INCREMENT,
    specialty_id INT NOT NULL UNIQUE,
    name         VARCHAR(80),
    primary key (id)
);

CREATE TABLE if NOT EXISTS vet_specialties
(
    vet_id       INT NOT NULL,
    specialty_id INT NOT NULL
);
