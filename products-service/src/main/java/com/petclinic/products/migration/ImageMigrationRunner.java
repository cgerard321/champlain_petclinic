package com.petclinic.products.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
@Order(-100)
@RequiredArgsConstructor
@Slf4j
public class ImageMigrationRunner implements CommandLineRunner {
    private final LegacyImageMigration migration;
    private final ImageMigrationStore store;

    @Value("${app.image-migration.enabled:false}")
    private boolean enabled;

    @Override
    public void run(String... args) {
        if (enabled) migrate();
        Long remaining = store.remainingLegacyReferences().block(Duration.ofMinutes(1));
        if (remaining == null || remaining > 0) {
            throw new IllegalStateException("Products still reference legacy images. Run with app.image-migration.enabled=true before cutover.");
        }
    }

    private void migrate() {
        var results = migration.migrate().doOnNext(result -> {
            if (result.success()) log.info("Image migration {}: {}", result.legacyImageId(), result.message());
            else log.error("Image migration {} FAILED: {}", result.legacyImageId(), result.message());
        }).collectList().block(Duration.ofMinutes(30));
        if (results == null || results.stream().anyMatch(result -> !result.success())) {
            throw new IllegalStateException("Image migration incomplete. Originals retained; inspect the journal before cutover.");
        }
        log.info("Processed {} legacy images. Original bytes retained for recovery.", results.size());
    }
}
