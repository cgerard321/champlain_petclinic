DROP TABLE billings IF EXISTS;

CREATE TABLE billings (
                          id   INTEGER IDENTITY PRIMARY KEY,
                          customerId INT,
                          date DATE,
                          visitType VARCHAR(80),
                          amount DOUBLE
);
