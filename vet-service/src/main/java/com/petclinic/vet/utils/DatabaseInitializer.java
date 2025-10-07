package com.petclinic.vet.utils;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DatabaseInitializer {

    @Autowired
    public DatabaseInitializer(ConnectionFactory connectionFactory) {
        initializeDatabase(connectionFactory).subscribe();
    }

    private Mono<Void> initializeDatabase(ConnectionFactory connectionFactory) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> {
                    // Create the albums table
                    String createAlbumsTableSql = "CREATE TABLE IF NOT EXISTS albums (" +
                            "id SERIAL PRIMARY KEY, " +
                            "vet_id VARCHAR(255) NOT NULL, " +
                            "filename VARCHAR(255), " +
                            "img_type VARCHAR(50), " +
                            "img_data BYTEA " +
                            ")";

                    // Update the images table to include an album_id foreign key
                    String createImagesTableSql = "CREATE TABLE IF NOT EXISTS images (" +
                            "id SERIAL PRIMARY KEY, " +// New foreign key to the albums table
                            "vet_id VARCHAR(255) NOT NULL, " +
                            "filename VARCHAR(255), " +
                            "img_type VARCHAR(20), " +
                            "img_data BYTEA " +
                            ")";

                    // Badges table remains the same
                    String createBadgesTableSql = "CREATE TABLE IF NOT EXISTS badges (" +
                            "id SERIAL PRIMARY KEY, " +
                            "vet_id VARCHAR(255) UNIQUE, " +
                            "badge_title VARCHAR(255), " +
                            "badge_date VARCHAR(255), " +
                            "img_data BYTEA" +
                            ")";

                    // Execute the SQL statements to create the tables
                    return Mono.from(connection.createStatement(createAlbumsTableSql).execute())
                            .then(Mono.from(connection.createStatement(createImagesTableSql).execute()))
                            .then(Mono.from(connection.createStatement(createBadgesTableSql).execute()))
                            .then(Mono.from(connection.close()));
                });
    }
}
