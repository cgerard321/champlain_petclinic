package com.petclinic.vet.util;

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
                    String createTableSql = "CREATE TABLE IF NOT EXISTS images (id SERIAL PRIMARY KEY, vet_id VARCHAR(10) UNIQUE, filename VARCHAR(255) UNIQUE, img_type VARCHAR(20), img_data BYTEA)";
                    return Mono.from(connection.createStatement(createTableSql).execute()).then(Mono.from(connection.close()));
                });
    }
}
