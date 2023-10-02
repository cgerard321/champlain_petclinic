DROP TABLE IF EXISTS images;

CREATE TABLE IF NOT EXISTS images(
    id SERIAL,
    vet_id varchar(10) unique,
    filename varchar(255) unique,
    img_type varchar(10),
    img_data bytea,
    PRIMARY KEY (id)
);