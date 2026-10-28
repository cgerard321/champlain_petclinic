package com.petclinic.products.migration;

import com.petclinic.products.datalayer.images.Image;
import com.petclinic.products.datalayer.images.ImageRepository;
import com.petclinic.products.domainclientlayer.FileResponseDTO;
import com.petclinic.products.domainclientlayer.FilesServiceClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SeedImagePublishingTest {
    private final ImageRepository originals = mock(ImageRepository.class);
    private final ImageMigrationStore store = mock(ImageMigrationStore.class);
    private final FilesServiceClient files = mock(FilesServiceClient.class);
    private final LegacyImageMigration publisher = new LegacyImageMigration(originals, store, files);
    private final Image seed = Image.builder().imageId("built-in").imageName("dog.png")
            .imageType("image/png").imageData(new byte[]{1, 2, 3}).build();
    private final FileResponseDTO file = FileResponseDTO.builder().fileId("new-file")
            .fileName("dog").fileType("image/png").fileData(new byte[]{1, 2, 3}).build();

    @Test
    void publishesAndVerifiesSeedWithoutWritingLegacyStorage() {
        when(store.prepare("seed:built-in")).thenReturn(Mono.empty());
        when(store.find("seed:built-in")).thenReturn(
                Mono.just(new ImageMigrationStore.Entry("PENDING", null)),
                Mono.just(new ImageMigrationStore.Entry("VERIFIED", "new-file")));
        when(store.claim("seed:built-in")).thenReturn(Mono.just(true));
        when(store.uploaded("seed:built-in", "new-file")).thenReturn(Mono.empty());
        when(store.linkProducts("seed:built-in", "new-file")).thenReturn(Mono.empty());
        when(files.addFile(any())).thenReturn(Mono.just(file));
        when(files.getFile("new-file")).thenReturn(Mono.just(file));

        StepVerifier.create(publisher.publishSeed(seed)).expectNext("new-file").verifyComplete();
        verify(files).addFile(any());
        verifyNoInteractions(originals);
    }

    @org.junit.jupiter.api.BeforeEach
    void allowVerifiedRelinking() {
        when(store.linkProducts("seed:built-in", "new-file")).thenReturn(Mono.empty());
    }

    @Test
    void retryAfterProductSaveFailureReusesUploadedSeed() {
        when(store.prepare("seed:built-in")).thenReturn(Mono.empty());
        when(store.find("seed:built-in")).thenReturn(Mono.just(new ImageMigrationStore.Entry("VERIFIED", "new-file")));
        when(files.getFile("new-file")).thenReturn(Mono.just(file));
        StepVerifier.create(publisher.publishSeed(seed)).expectNext("new-file").verifyComplete();
        verify(files, never()).addFile(any());
        verifyNoInteractions(originals);
    }

    @Test
    void missingCompletedSeedDoesNotCreateAnotherUpload() {
        when(store.prepare("seed:built-in")).thenReturn(Mono.empty());
        when(store.find("seed:built-in")).thenReturn(Mono.just(new ImageMigrationStore.Entry("VERIFIED", "new-file")));
        when(files.getFile("new-file")).thenReturn(Mono.empty());
        StepVerifier.create(publisher.publishSeed(seed)).expectError(IllegalStateException.class).verify();
        verify(files, never()).addFile(any());
    }
    @Test
    void deletedCompletedSeedIsSkippedWithoutBlockingOrReuploading() {
        when(store.prepare("seed:built-in")).thenReturn(Mono.empty());
        when(store.find("seed:built-in")).thenReturn(Mono.just(new ImageMigrationStore.Entry("VERIFIED", "new-file")));
        when(files.getFile("new-file")).thenReturn(Mono.error(
                new com.petclinic.products.utils.exceptions.FileNotFoundInFilesServiceException("Deleted")));
        StepVerifier.create(publisher.publishSeed(seed)).verifyComplete();
        verify(files, never()).addFile(any());
    }

    @Test
    void completedSeedOutageStillFailsInsteadOfTreatingItAsDeletion() {
        when(store.prepare("seed:built-in")).thenReturn(Mono.empty());
        when(store.find("seed:built-in")).thenReturn(Mono.just(new ImageMigrationStore.Entry("VERIFIED", "new-file")));
        when(files.getFile("new-file")).thenReturn(Mono.error(new IllegalStateException("Unavailable")));
        StepVerifier.create(publisher.publishSeed(seed)).expectError(IllegalStateException.class).verify();
        verify(files, never()).addFile(any());
    }

}
