DROP TABLE vet_specialties IF EXISTS;
DROP TABLE vet IF EXISTS;
DROP TABLE specialties IF EXISTS;

CREATE TABLE vet
(
    id           INTEGER PRIMARY KEY,
    vet_id       INTEGER,
    first_name   VARCHAR(30),
    last_name    VARCHAR(30),
    email        VARCHAR(100),
    phone_number VARCHAR(30),
    image        LONGBLOB,
    resume       VARCHAR(350),
    workday      VARCHAR(250),
    is_active    BOOLEAN
);
CREATE INDEX vets_last_name ON vet (last_name);

CREATE TABLE specialties
(
    id           INTEGER PRIMARY KEY,
    specialty_id INTEGER,
    name         VARCHAR(80)
);
CREATE INDEX specialties_name ON specialties (name);

CREATE TABLE vet_specialties
(
    vet_id       INTEGER NOT NULL,
    specialty_id INTEGER NOT NULL
);
ALTER TABLE vet_specialties
    ADD CONSTRAINT fk_vet_specialties_vets FOREIGN KEY (vet_id) REFERENCES vet (id);
ALTER TABLE vet_specialties
    ADD CONSTRAINT fk_vet_specialties_specialties FOREIGN KEY (specialty_id) REFERENCES specialties (id);
