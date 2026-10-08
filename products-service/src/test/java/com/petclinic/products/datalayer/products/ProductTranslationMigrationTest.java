package com.petclinic.products.datalayer.products;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class ProductTranslationMigrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Test
    void upgradesExistingRowsWithoutOverwritingEmployeeContentOnRestart() throws Exception {
        String schema;
        try (var input = getClass().getResourceAsStream("/schema.sql")) {
            assertNotNull(input);
            schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (var connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             var sql = connection.createStatement()) {
            // Reproduce a pre-localization database before executing the startup schema.
            sql.execute(schema.substring(0, schema.indexOf("-- CPC-1937")));
            sql.execute("INSERT INTO products(product_id, product_name, product_description, product_quantity) VALUES "
                    + "('06a7d573-bcab-4db3-956f-773324b92a80','Dog Food','Premium dry food for adult dogs',44),"
                    + "('98f7b33a-d62a-420a-a84a-05a27c85fc91','Custom cat product','Custom description',3)");
            sql.execute(schema);
            try (var rows = sql.executeQuery("SELECT * FROM products WHERE product_name='Dog Food'")) {
                assertTrue(rows.next());
                assertEquals("Nourriture pour chiens", rows.getString("product_name_fr"));
                assertEquals("Nourriture sèche de qualité supérieure pour chiens adultes", rows.getString("product_description_fr"));
                assertEquals(44, rows.getInt("product_quantity"));
            }
            sql.execute("UPDATE products SET product_name_fr='Nom personnalisé', product_description_fr='' WHERE product_name='Dog Food'");
            sql.execute(schema);
            try (var rows = sql.executeQuery("SELECT * FROM products WHERE product_name='Dog Food'")) {
                assertTrue(rows.next());
                assertEquals("Nom personnalisé", rows.getString("product_name_fr"));
                assertEquals("", rows.getString("product_description_fr"));
            }
            try (var rows = sql.executeQuery("SELECT * FROM products WHERE product_name='Custom cat product'")) {
                assertTrue(rows.next());
                assertNull(rows.getString("product_name_fr"));
                assertNull(rows.getString("product_description_fr"));
                assertEquals(3, rows.getInt("product_quantity"));
            }
        }
    }
}
