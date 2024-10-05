DROP TABLE IF EXISTS images;

CREATE TABLE IF NOT EXISTS images(
     id SERIAL PRIMARY KEY,
     vet_id VARCHAR(255) NOT NULL,
    filename VARCHAR(255),
    img_type VARCHAR(50),
    img_data BYTEA
);
DROP TABLE IF EXISTS albums;
CREATE TABLE albums (
     id SERIAL PRIMARY KEY,
     vet_id VARCHAR(255) NOT NULL,
     filename VARCHAR(255),
     img_type VARCHAR(50),
     img_data BYTEA
);


DROP TABLE IF EXISTS badges;
CREATE TABLE IF NOT EXISTS badges (
    id SERIAL,
    vet_id varchar(255),
    badge_title varchar(255),
    badge_date varchar(255),
    img_data bytea,
    PRIMARY KEY (id),
    UNIQUE (vet_id)
);




