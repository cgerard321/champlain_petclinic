DROP TABLE visits IF EXISTS;

CREATE TABLE visits (
                        id          INTEGER IDENTITY PRIMARY KEY,
                        visit_id    VARCHAR(36) NOT NULL UNIQUE,
                        pet_id      INTEGER NOT NULL,
                        visit_date  DATE,
                        description VARCHAR(8192),
                        practitioner_id INTEGER NOT NULL,
                        status BIT 
);

CREATE INDEX visits_pet_id ON visits (pet_id);
