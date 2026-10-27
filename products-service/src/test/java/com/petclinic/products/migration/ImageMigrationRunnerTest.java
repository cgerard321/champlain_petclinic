package com.petclinic.products.migration;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageMigrationRunnerTest {
    private final LegacyImageMigration migration = mock(LegacyImageMigration.class);
    private final ImageMigrationStore store = mock(ImageMigrationStore.class);
    private final ImageMigrationRunner runner = new ImageMigrationRunner(migration, store);

    @Test
    void refusesCutoverWithUnmigratedReferences() {
        when(store.remainingLegacyReferences()).thenReturn(Mono.just(1L));
        assertThrows(IllegalStateException.class, runner::run);
        verifyNoInteractions(migration);
    }

    @Test
    void freshOrMigratedDatabaseStartsWithoutMigration() {
        when(store.remainingLegacyReferences()).thenReturn(Mono.just(0L));
        assertDoesNotThrow(() -> { runner.run(); });
        verifyNoInteractions(migration);
    }

    @Test
    void failedMigrationStopsStartup() {
        ReflectionTestUtils.setField(runner, "enabled", true);
        when(migration.migrate()).thenReturn(Flux.just(new LegacyImageMigration.Result("old", false, "failed upload")));
        assertThrows(IllegalStateException.class, runner::run);
        verifyNoInteractions(store);
    }
}
