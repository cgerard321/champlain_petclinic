DROP TABLE billings IF EXISTS;

CREATE TABLE billings (
                          id   INTEGER IDENTITY PRIMARY KEY,
                          date DATE,
                          visit_type VARCHAR(80),
                          amount DOUBLE
);
CREATE INDEX billings_visit_type ON billings (visit_type);