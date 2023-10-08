DROP TABLE IF EXISTS images;

CREATE TABLE IF NOT EXISTS images(
    id SERIAL,
    vet_id varchar(36) unique,
    filename varchar(255) unique,
    img_type varchar(10),
    img_data bytea,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS badges;
CREATE TABLE IF NOT EXISTS badges (
    id INT,
    vet_id varchar(255),
    badge_title varchar(255),
    badge_date varchar(255),
    img_data bytea,
    PRIMARY KEY (id),
    UNIQUE (vet_id)
);




