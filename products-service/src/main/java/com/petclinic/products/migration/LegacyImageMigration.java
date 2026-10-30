package com.petclinic.products.migration;

import com.petclinic.products.datalayer.images.Image;
import com.petclinic.products.datalayer.images.ImageRepository;
import com.petclinic.products.domainclientlayer.FileRequestDTO;
import com.petclinic.products.domainclientlayer.FileResponseDTO;
import com.petclinic.products.domainclientlayer.FilesServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Arrays;
import com.petclinic.products.utils.exceptions.FileNotFoundInFilesServiceException;

/** Automatic legacy migration; originals are retained until deployment verification. */
@Service
@RequiredArgsConstructor
public class LegacyImageMigration {
    private final ImageRepository images;
    private final ImageMigrationStore store;
    private final FilesServiceClient files;

    public Flux<Result> migrate() {
        return images.findAll().concatMap(image -> migrateImage(image)
                .onErrorResume(error -> Mono.just(new Result(image.getImageId(), false, error.getMessage()))));
    }

    /** Journal seed uploads too, so a failed product save does not cause duplicate uploads. */
    public Mono<String> publishSeed(Image seed) {
        Image source = Image.builder().imageId("seed:" + seed.getImageId())
                .imageName(seed.getImageName()).imageType(seed.getImageType())
                .imageData(seed.getImageData()).build();
        return migrateImage(source).then(store.find(source.getImageId()))
                .flatMap(entry -> files.getFile(entry.fileId())
                        .filter(file -> entry.fileId().equals(file.getFileId())
                                && file.getFileData() != null && file.getFileData().length > 0)
                        .switchIfEmpty(Mono.error(new IllegalStateException("Seed file is missing; reconcile before retrying")))
                        .map(file -> entry.fileId())
                        // A completed seed deleted with its product must not be recreated.
                        // Only an explicit 404 confirms deletion; outages still fail startup.
                        .onErrorResume(FileNotFoundInFilesServiceException.class, error ->
                                "VERIFIED".equals(entry.state()) ? Mono.empty() : Mono.error(error)));
    }

    Mono<Result> migrateImage(Image image) {
        if (image.getImageId() == null || image.getImageId().isBlank()
                || image.getImageData() == null || image.getImageData().length == 0
                || image.getImageName() == null || image.getImageName().isBlank()
                || image.getImageType() == null || image.getImageType().isBlank()) {
            return Mono.error(new IllegalStateException("Legacy image has incomplete metadata or bytes"));
        }
        return store.prepare(image.getImageId())
                .then(store.find(image.getImageId()))
                .flatMap(entry -> {
                    if ("VERIFIED".equals(entry.state()) && entry.fileId() != null) {
                        // A migrated file may since have been intentionally edited or deleted.
                        // Migration reruns must not restore its old bytes or reject that edit.
                        return store.linkProducts(image.getImageId(), entry.fileId())
                                .thenReturn(new Result(image.getImageId(), true, "Already migrated to " + entry.fileId()));
                    }
                    if (entry.fileId() != null) {
                        return verifyAndLink(image, entry.fileId());
                    }
                    if (!"PENDING".equals(entry.state())) {
                        return Mono.error(new IllegalStateException(
                                "Upload outcome is uncertain; reconcile the migration journal before retrying"));
                    }
                    return store.claim(image.getImageId()).flatMap(claimed -> {
                        if (!claimed) return Mono.error(new IllegalStateException("Image is already claimed by another run"));
                        return files.addFile(FileRequestDTO.builder()
                                        .fileName(image.getImageName()).fileType(canonicalImageType(image.getImageType()))
                                        .fileData(image.getImageData()).build())
                                .switchIfEmpty(Mono.error(new IllegalStateException("Upload returned no file")))
                                .flatMap(file -> {
                                    if (file.getFileId() == null || file.getFileId().isBlank()
                                            || file.getFileId().equals(image.getImageId())) {
                                        return Mono.error(new IllegalStateException("Upload returned an invalid new file ID"));
                                    }
                                    return store.uploaded(image.getImageId(), file.getFileId())
                                            .then(verifyAndLink(image, file.getFileId()));
                                });
                    });
                });
    }

    private Mono<Result> verifyAndLink(Image image, String fileId) {
        return files.getFile(fileId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Uploaded file cannot be read back")))
                .flatMap(file -> {
                    if (!matches(image, file, fileId)) {
                        return Mono.error(new IllegalStateException("Uploaded file failed byte/type verification"));
                    }
                    return store.linkProducts(image.getImageId(), fileId)
                            .thenReturn(new Result(image.getImageId(), true, "Verified and linked to " + fileId));
                });
    }

    private boolean matches(Image image, FileResponseDTO file, String fileId) {
        return fileId.equals(file.getFileId()) && canonicalImageType(image.getImageType()).equalsIgnoreCase(canonicalImageType(file.getFileType()))
                && Arrays.equals(image.getImageData(), file.getFileData());
    }

    private String canonicalImageType(String type) {
        return "image/jpg".equalsIgnoreCase(type) ? "image/jpeg" : type;
    }

    public record Result(String legacyImageId, boolean success, String message) {}
}
