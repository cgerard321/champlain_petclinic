DROP TABLE visits IF EXISTS;

CREATE TABLE visits (
                        id          INTEGER IDENTITY PRIMARY KEY,
                        pet_id      INTEGER NOT NULL,
                        visit_date  DATE,
                        description VARCHAR(8192),
                        practitioner_id INTEGER NOT NULL
);

CREATE INDEX visits_pet_id ON visits (pet_id);
