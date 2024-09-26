DROP TABLE IF EXISTS images;

CREATE TABLE IF NOT EXISTS images(
    id SERIAL,
    vet_id varchar(36) unique,
    filename varchar(255),
    img_type varchar(10),
    img_base64 TEXT,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS badges;
CREATE TABLE IF NOT EXISTS badges (
    id SERIAL,
    vet_id varchar(255),
    badge_title varchar(255),
    badge_date varchar(255),
    img_base64 TEXT,
    PRIMARY KEY (id),
    UNIQUE (vet_id)
);




