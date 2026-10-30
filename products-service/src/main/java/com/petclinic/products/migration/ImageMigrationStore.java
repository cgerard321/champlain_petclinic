package com.petclinic.products.migration;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

@Repository
public class ImageMigrationStore {
    private final DatabaseClient database;
    private final TransactionalOperator transaction;

    public ImageMigrationStore(DatabaseClient database, ReactiveTransactionManager transactionManager) {
        this.database = database;
        this.transaction = TransactionalOperator.create(transactionManager);
    }

    public record Entry(String state, String fileId) {}

    public Mono<Long> pendingLegacyImages() {
        return database.sql("SELECT COUNT(*) AS pending FROM images i WHERE NOT EXISTS (SELECT 1 FROM product_image_migration m WHERE m.legacy_image_id = i.image_id AND m.state = 'VERIFIED' AND m.file_id IS NOT NULL) OR EXISTS (SELECT 1 FROM products p WHERE p.image_id = i.image_id)")
                .map((row, metadata) -> row.get("pending", Long.class)).one();
    }

    public Mono<String> resolveFileId(String imageId) {
        return database.sql("SELECT file_id FROM product_image_migration WHERE legacy_image_id = :id AND state = 'VERIFIED' AND file_id IS NOT NULL")
                .bind("id", imageId).map((row, metadata) -> row.get("file_id", String.class))
                .one().defaultIfEmpty(imageId);
    }

    public Mono<Long> remainingLegacyReferences() {
        return database.sql("SELECT COUNT(*) AS remaining FROM products p WHERE EXISTS (SELECT 1 FROM images i WHERE i.image_id = p.image_id)")
                .map((row, metadata) -> row.get("remaining", Long.class)).one();
    }

    public Mono<Void> prepare(String oldId) {
        return database.sql("INSERT INTO product_image_migration(legacy_image_id, state) VALUES (:id, 'PENDING') ON CONFLICT DO NOTHING")
                .bind("id", oldId).fetch().rowsUpdated().then();
    }

    public Mono<Entry> find(String oldId) {
        return database.sql("SELECT state, file_id FROM product_image_migration WHERE legacy_image_id = :id")
                .bind("id", oldId).map((row, metadata) -> new Entry(row.get("state", String.class), row.get("file_id", String.class)))
                .one().switchIfEmpty(Mono.error(new IllegalStateException("Missing migration journal entry")));
    }

    public Mono<Boolean> claim(String oldId) {
        return database.sql("UPDATE product_image_migration SET state = 'UPLOADING' WHERE legacy_image_id = :id AND state = 'PENDING'")
                .bind("id", oldId).fetch().rowsUpdated().map(count -> count == 1);
    }

    public Mono<Void> uploaded(String oldId, String fileId) {
        return database.sql("UPDATE product_image_migration SET file_id = :file, state = 'UPLOADED' WHERE legacy_image_id = :id AND state = 'UPLOADING'")
                .bind("id", oldId).bind("file", fileId).fetch().rowsUpdated()
                .flatMap(count -> count == 1 ? Mono.empty() : Mono.error(new IllegalStateException("Upload journal update failed")));
    }

    public Mono<Void> linkProducts(String oldId, String fileId) {
        // Conditional update preserves products whose image was changed by an employee during migration.
        return database.sql("UPDATE products SET image_id = :file WHERE image_id = :id")
                .bind("id", oldId).bind("file", fileId).fetch().rowsUpdated()
                .then(database.sql("UPDATE product_image_migration SET state = 'VERIFIED' WHERE legacy_image_id = :id AND file_id = :file")
                        .bind("id", oldId).bind("file", fileId).fetch().rowsUpdated())
                .flatMap(count -> count == 1 ? Mono.<Void>empty() : Mono.error(new IllegalStateException("Verification journal update failed")))
                .as(transaction::transactional);
    }
}
