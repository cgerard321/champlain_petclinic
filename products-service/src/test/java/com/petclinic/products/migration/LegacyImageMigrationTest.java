package com.petclinic.products.migration;

import com.petclinic.products.datalayer.images.Image;
import com.petclinic.products.datalayer.images.ImageRepository;
import com.petclinic.products.domainclientlayer.FileResponseDTO;
import com.petclinic.products.domainclientlayer.FilesServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LegacyImageMigrationTest {
    private final ImageRepository images = mock(ImageRepository.class);
    private final ImageMigrationStore store = mock(ImageMigrationStore.class);
    private final FilesServiceClient files = mock(FilesServiceClient.class);
    private final LegacyImageMigration migration = new LegacyImageMigration(images, store, files);
    private final Image source = Image.builder().imageId("legacy").imageName("dog.png")
            .imageType("image/png").imageData(new byte[]{1, 2, 3}).build();
    private final FileResponseDTO uploaded = FileResponseDTO.builder().fileId("new-file")
            .fileName("dog").fileType("image/png").fileData(new byte[]{1, 2, 3}).build();

    @BeforeEach
    void setUp() {
        when(images.findAll()).thenReturn(Flux.just(source));
        when(store.prepare("legacy")).thenReturn(Mono.empty());
        when(store.find("legacy")).thenReturn(Mono.just(new ImageMigrationStore.Entry("PENDING", null)));
        when(store.claim("legacy")).thenReturn(Mono.just(true));
        when(store.uploaded("legacy", "new-file")).thenReturn(Mono.empty());
        when(store.linkProducts("legacy", "new-file")).thenReturn(Mono.empty());
        when(files.addFile(any())).thenReturn(Mono.just(uploaded));
        when(files.getFile("new-file")).thenReturn(Mono.just(uploaded));
    }

    @Test
    void jpgAliasUploadsAsJpegAndVerifiesWithoutChangingOriginal() {
        source.setImageType("image/jpg");
        uploaded.setFileType("image/jpeg");
        StepVerifier.create(migration.migrate()).expectNextMatches(LegacyImageMigration.Result::success).verifyComplete();
        verify(files).addFile(argThat(request -> "image/jpeg".equals(request.getFileType())));
        org.junit.jupiter.api.Assertions.assertEquals("image/jpg", source.getImageType());
    }

    @Test
    void uploadsVerifiesThenLinksWithoutDeletingOriginals() {
        StepVerifier.create(migration.migrate()).expectNextMatches(LegacyImageMigration.Result::success).verifyComplete();
        var order = inOrder(store, files);
        order.verify(store).claim("legacy");
        order.verify(files).addFile(argThat(request -> java.util.Arrays.equals(request.getFileData(), source.getImageData())));
        order.verify(store).uploaded("legacy", "new-file");
        order.verify(files).getFile("new-file");
        order.verify(store).linkProducts("legacy", "new-file");
        verify(images).findAll();
        verify(files, never()).deleteFile(anyString());
    }

    @Test
    void failedUploadDoesNotLinkOrDeleteSource() {
        when(files.addFile(any())).thenReturn(Mono.error(new IllegalStateException("connection lost")));
        expectFailure();
        verify(store, never()).uploaded(anyString(), anyString());
        verify(store, never()).linkProducts(anyString(), anyString());
        verify(images).findAll();
    }

    @Test
    void uncertainUploadIsNotBlindlyRepeated() {
        when(store.find("legacy")).thenReturn(Mono.just(new ImageMigrationStore.Entry("UPLOADING", null)));
        expectFailure();
        verifyNoInteractions(files);
    }

    @Test
    void resumedUploadReusesSavedFileId() {
        when(store.find("legacy")).thenReturn(Mono.just(new ImageMigrationStore.Entry("UPLOADED", "new-file")));
        StepVerifier.create(migration.migrate()).expectNextMatches(LegacyImageMigration.Result::success).verifyComplete();
        verify(files, never()).addFile(any());
        verify(store).linkProducts("legacy", "new-file");
    }

    @Test
    void completedRunCanBeRepeatedWithoutUploadingAgain() {
        when(store.find("legacy")).thenReturn(Mono.just(new ImageMigrationStore.Entry("VERIFIED", "new-file")));
        StepVerifier.create(migration.migrate()).expectNextMatches(LegacyImageMigration.Result::success).verifyComplete();
        verifyNoInteractions(files);
        verify(store).linkProducts("legacy", "new-file");
    }

    @Test
    void corruptedReadbackDoesNotUpdateProducts() {
        uploaded.setFileData(new byte[]{9});
        expectFailure();
        verify(store, never()).linkProducts(anyString(), anyString());
    }

    @Test
    void missingReadbackDoesNotUpdateProducts() {
        when(files.getFile("new-file")).thenReturn(Mono.empty());
        expectFailure();
        verify(store, never()).linkProducts(anyString(), anyString());
    }

    @Test
    void concurrentClaimLoserDoesNotUpload() {
        when(store.claim("legacy")).thenReturn(Mono.just(false));
        expectFailure();
        verifyNoInteractions(files);
    }

    @Test
    void journalFailureStopsBeforeProductsAreChanged() {
        when(store.uploaded("legacy", "new-file")).thenReturn(Mono.error(new IllegalStateException("database unavailable")));
        expectFailure();
        verify(store, never()).linkProducts(anyString(), anyString());
    }

    @Test
    void incompleteSourceDoesNotStartUpload() {
        source.setImageData(null);
        expectFailure();
        verifyNoInteractions(files, store);
    }

    private void expectFailure() {
        StepVerifier.create(migration.migrate()).expectNextMatches(result -> !result.success()).verifyComplete();
    }
}
