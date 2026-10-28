package com.petclinic.products.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import com.petclinic.products.domainclientlayer.FilesServiceClient;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageMigrationRunnerTest {
    private final LegacyImageMigration migration = mock(LegacyImageMigration.class);
    private final ImageMigrationStore store = mock(ImageMigrationStore.class);
    private final FilesServiceClient files = mock(FilesServiceClient.class);
    private final ImageMigrationRunner runner = new ImageMigrationRunner(migration, store, files);

    @BeforeEach
    void filesAreReady() {
        when(files.awaitStartup()).thenReturn(Mono.empty());
    }

    @Test
    void unavailableFilesStopsBeforeAnyMigrationWrites() {
        ReflectionTestUtils.setField(runner, "enabled", true);
        when(files.awaitStartup()).thenReturn(Mono.error(new IllegalStateException("Unavailable")));
        assertThrows(IllegalStateException.class, runner::run);
        verifyNoInteractions(migration, store);
    }

    @Test
    void enabledDeploymentWaitsThenMigratesAndVerifiesCutover() {
        ReflectionTestUtils.setField(runner, "enabled", true);
        when(migration.migrate()).thenReturn(Flux.just(new LegacyImageMigration.Result("old", true, "verified")));
        when(store.remainingLegacyReferences()).thenReturn(Mono.just(0L));
        runner.run();
        var order = inOrder(files, migration, store);
        order.verify(files).awaitStartup();
        order.verify(migration).migrate();
        order.verify(store).remainingLegacyReferences();
    }

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
