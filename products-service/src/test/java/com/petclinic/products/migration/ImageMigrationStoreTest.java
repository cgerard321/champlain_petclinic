package com.petclinic.products.migration;

import io.r2dbc.spi.ConnectionFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class ImageMigrationStoreTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    private ImageMigrationStore store;
    private DatabaseClient database;

    @BeforeEach
    void setup() throws Exception {
        try (var input = getClass().getResourceAsStream("/schema.sql");
             var connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             var statement = connection.createStatement()) {
            assertNotNull(input);
            statement.execute(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            statement.execute("DELETE FROM product_image_migration; DELETE FROM products;");
            statement.execute("INSERT INTO products(product_id, image_id) VALUES ('one', 'legacy'), ('two', 'legacy'), ('edited', 'employee-file')");
        }
        var connectionFactory = ConnectionFactories.get("r2dbc:postgresql://" + postgres.getUsername() + ":"
                + postgres.getPassword() + "@" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName());
        database = DatabaseClient.create(connectionFactory);
        store = new ImageMigrationStore(database, new R2dbcTransactionManager(connectionFactory));
    }

    @Test
    void onlyOneRunCanClaimImage() {
        store.prepare("legacy").block();
        assertEquals(true, store.claim("legacy").block());
        store.prepare("legacy").block();
        assertEquals(false, store.claim("legacy").block());
        assertEquals("UPLOADING", store.find("legacy").block().state());
    }

    @Test
    void linksSharedProductsAndPreservesEmployeeChangesOnRerun() {
        prepareUploaded();
        store.linkProducts("legacy", "new-file").block();
        assertEquals("new-file", imageId("one"));
        assertEquals("new-file", imageId("two"));
        assertEquals("employee-file", imageId("edited"));
        database.sql("UPDATE products SET image_id = 'employee-new' WHERE product_id = 'one'").fetch().rowsUpdated().block();
        store.linkProducts("legacy", "new-file").block();
        assertEquals("employee-new", imageId("one"));
        assertEquals("VERIFIED", store.find("legacy").block().state());
    }

    @Test
    void failedJournalVerificationRollsBackProductChanges() {
        prepareUploaded();
        StepVerifier.create(store.linkProducts("legacy", "wrong-id")).expectError(IllegalStateException.class).verify();
        assertEquals("legacy", imageId("one"));
        assertEquals("legacy", imageId("two"));
        assertEquals("UPLOADED", store.find("legacy").block().state());
    }

    @Test
    void prepareDoesNotEraseConfirmedUpload() {
        prepareUploaded();
        store.prepare("legacy").block();
        assertEquals(new ImageMigrationStore.Entry("UPLOADED", "new-file"), store.find("legacy").block());
    }

    private void prepareUploaded() {
        store.prepare("legacy").then(store.claim("legacy")).block();
        store.uploaded("legacy", "new-file").block();
    }

    private String imageId(String productId) {
        return database.sql("SELECT image_id FROM products WHERE product_id = :id").bind("id", productId)
                .map((row, metadata) -> row.get("image_id", String.class)).one().block();
    }
}
