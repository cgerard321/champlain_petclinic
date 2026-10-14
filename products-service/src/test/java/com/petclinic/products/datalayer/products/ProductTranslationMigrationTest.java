package com.petclinic.products.datalayer.products;

import org.junit.jupiter.api.Test;
import com.petclinic.products.utils.DataLoaderService;
import io.r2dbc.spi.ConnectionFactories;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
            sql.execute("INSERT INTO products(product_id, product_name, product_description, product_quantity, image_id) VALUES "
                    + "('06a7d573-bcab-4db3-956f-773324b92a80','Dog Food','Premium dry food for adult dogs',44,''),"
                    + "('98f7b33a-d62a-420a-a84a-05a27c85fc91','Custom cat product','Custom description',3,''),"
                    + "('b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7001','Senior Dog Joint Supplement','Glucosamine chews supporting mobility in older dogs',18,'')");
            sql.execute(schema);
            var template = new R2dbcEntityTemplate(ConnectionFactories.get(
                    "r2dbc:postgresql://" + postgres.getUsername() + ":" + postgres.getPassword()
                            + "@" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName()));
            var repository = new R2dbcRepositoryFactory(template).getRepository(ProductRepository.class);
            var loader = new DataLoaderService();
            ReflectionTestUtils.setField(loader, "productRepository", repository);
            // The migration test exercises the real product repository, while the
            // unrelated seed repositories are mocked so seeding can reach the
            // translation backfill without requiring a complete fixture database.
            var productTypeRepository = mock(ProductTypeRepository.class);
            when(productTypeRepository.findByProductTypeId(anyString())).thenReturn(Mono.empty());
            when(productTypeRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
            var imageRepository = mock(com.petclinic.products.datalayer.images.ImageRepository.class);
            when(imageRepository.findImageByImageId(anyString()))
                    .thenReturn(Mono.just(com.petclinic.products.datalayer.images.Image.builder().build()));
            var ratingRepository = mock(com.petclinic.products.datalayer.ratings.RatingRepository.class);
            when(ratingRepository.findRatingByCustomerIdAndProductId(anyString(), anyString()))
                    .thenReturn(Mono.empty());
            when(ratingRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
            var bundleRepository = mock(ProductBundleRepository.class);
            when(bundleRepository.findAllByProductIdsContaining(anyString())).thenReturn(Flux.empty());
            when(bundleRepository.findByBundleId(anyString())).thenReturn(Mono.empty());
            when(bundleRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
            ReflectionTestUtils.setField(loader, "productTypeRepository", productTypeRepository);
            ReflectionTestUtils.setField(loader, "imageRepository", imageRepository);
            ReflectionTestUtils.setField(loader, "ratingRepository", ratingRepository);
            ReflectionTestUtils.setField(loader, "productBundleRepository", bundleRepository);
            // Schema only adds columns; the loader owns translation data.
            try (var rows = sql.executeQuery("SELECT product_name_fr FROM products WHERE product_name='Dog Food'")) {
                assertTrue(rows.next());
                assertNull(rows.getString(1));
            }
            loader.run();
            awaitFrenchTranslation(connection, "Dog Food", "Nourriture pour chiens");
            try (var rows = sql.executeQuery("SELECT * FROM products WHERE product_name='Dog Food'")) {
                assertTrue(rows.next());
                assertEquals("Nourriture pour chiens", rows.getString("product_name_fr"));
                assertEquals("Nourriture sèche de qualité supérieure pour chiens adultes", rows.getString("product_description_fr"));
                assertEquals(44, rows.getInt("product_quantity"));
            }
            awaitFrenchTranslation(connection, "Senior Dog Joint Supplement");
            try (var rows = sql.executeQuery("SELECT * FROM products WHERE product_id='b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7001'")) {
                assertTrue(rows.next());
                assertEquals("Supplément articulaire pour chiens âgés", rows.getString("product_name_fr"));
                assertEquals("Bouchées à la glucosamine favorisant la mobilité des chiens âgés", rows.getString("product_description_fr"));
                assertEquals(18, rows.getInt("product_quantity"));
            }
            sql.execute("UPDATE products SET product_name_fr='Nom personnalisé', product_description_fr='' WHERE product_name='Dog Food'");
            sql.execute(schema);
            loader.run();
            awaitFrenchTranslation(connection, "Dog Food");
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

    private void awaitFrenchTranslation(java.sql.Connection connection, String productName,
                                        String expectedName) throws Exception {
        long deadline = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < deadline) {
            try (var statement = connection.prepareStatement(
                    "SELECT product_name_fr FROM products WHERE product_name=?")) {
                statement.setString(1, productName);
                try (var rows = statement.executeQuery()) {
                    if (rows.next() && (expectedName == null || expectedName.equals(rows.getString(1)))) {
                        return;
                    }
                }
            }
            Thread.sleep(50);
        }
        fail("Timed out waiting for French translation of " + productName);
    }

    private void awaitFrenchTranslation(java.sql.Connection connection, String productName) throws Exception {
        awaitFrenchTranslation(connection, productName, null);
    }
}
