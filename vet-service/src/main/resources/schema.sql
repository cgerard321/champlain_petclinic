DROP TABLE IF EXISTS images;

CREATE TABLE IF NOT EXISTS images(
     id SERIAL PRIMARY KEY,   -- Foreign key referencing the album
     vet_id VARCHAR(255) NOT NULL,  -- Vet ID is stored here to simplify retrieval, adjust type if necessary
    filename VARCHAR(255),         -- Filename of the photo
    img_type VARCHAR(50),          -- Image type (e.g., jpeg, png)
    img_data BYTEA              -- Binary data of the image itself
);

CREATE TABLE albums (
     id SERIAL PRIMARY KEY,
     vet_id VARCHAR(255) NOT NULL,  -- Assuming vet_id is a VARCHAR type, adjust based on your needs
     filename VARCHAR(255),         -- Filename for the album's main cover image or representative file
     img_type VARCHAR(50),          -- Image type (if applicable)
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




