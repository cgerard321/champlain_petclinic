package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.dataaccesslayer.PromoRepository;
import com.petclinic.cartsservice.domainclientlayer.*;
import com.petclinic.cartsservice.utils.PromoImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PromoImageTest {
    private final PromoRepository repository = mock(PromoRepository.class);
    private final FilesServiceClient files = mock(FilesServiceClient.class);
    private final PromoCodeServiceImpl service = new PromoCodeServiceImpl(repository, files);
    private PromoCode promo;
    private PromoCodeRequestModel request;
    private FileRequestDTO image;
    private FileResponseDTO uploaded;

    @BeforeEach
    void setup() throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes);
        image = FileRequestDTO.builder().fileName("promo.png").fileType("image/png")
                .fileData(bytes.toByteArray()).build();
        uploaded = FileResponseDTO.builder().fileId("new-image").fileName(image.getFileName())
                .fileType(image.getFileType()).fileData(image.getFileData()).build();
        promo = PromoCode.builder().id("promo").imageId("old-image").scope("CART")
                .expirationDate(LocalDateTime.now().plusDays(1)).isActive(true).build();
        request = PromoCodeRequestModel.builder().Name("Summer").code("SUMMER")
                .expirationDate("2999-12-31T23:59:59").scope("CART").build();
    }

    @Test
    void createsPromotionWithUploadedImage() {
        request.setImage(image);
        when(files.addFile(image)).thenReturn(Mono.just(uploaded));
        when(repository.save(any())).thenAnswer(call -> Mono.just(call.getArgument(0)));
        StepVerifier.create(service.createPromo(request)).assertNext(result -> {
            assertEquals("new-image", result.getImageId());
            assertSame(uploaded, result.getImage());
        }).verifyComplete();
    }

    @Test
    void replacementDeletesOldImageAfterSavingNewReference() {
        request.setImage(image);
        when(repository.findById("promo")).thenReturn(Mono.just(promo));
        when(files.addFile(image)).thenReturn(Mono.just(uploaded));
        when(repository.save(any())).thenAnswer(call -> Mono.just(call.getArgument(0)));
        when(files.deleteFile("old-image")).thenReturn(Mono.empty());
        StepVerifier.create(service.updatePromoCodeById(request, "promo"))
                .assertNext(result -> assertEquals("new-image", result.getImageId())).verifyComplete();
        var order = inOrder(repository, files);
        order.verify(files).addFile(image);
        order.verify(repository).save(promo);
        order.verify(files).deleteFile("old-image");
    }

    @Test
    void removalClearsReferenceAndDeletesFile() {
        request.setRemoveImage(true);
        when(repository.findById("promo")).thenReturn(Mono.just(promo));
        when(repository.save(any())).thenAnswer(call -> Mono.just(call.getArgument(0)));
        when(files.deleteFile("old-image")).thenReturn(Mono.empty());
        StepVerifier.create(service.updatePromoCodeById(request, "promo")).assertNext(result -> {
            assertNull(result.getImageId());
            assertNull(result.getImage());
        }).verifyComplete();
        verify(files).deleteFile("old-image");
    }

    @Test
    void unchangedImageIsPreserved() {
        when(repository.findById("promo")).thenReturn(Mono.just(promo));
        when(repository.save(any())).thenAnswer(call -> Mono.just(call.getArgument(0)));
        when(files.getFile("old-image")).thenReturn(Mono.just(uploaded));
        StepVerifier.create(service.updatePromoCodeById(request, "promo"))
                .assertNext(result -> assertEquals("old-image", result.getImageId())).verifyComplete();
        verify(files, never()).addFile(any());
        verify(files, never()).deleteFile(any());
    }

    @Test
    void missingImageDoesNotHideActivePromotion() {
        when(repository.findAllByExpirationDateGreaterThanEqual(any())).thenReturn(Flux.just(promo));
        when(files.getFile("old-image")).thenReturn(Mono.error(new RuntimeException("Unavailable")));
        StepVerifier.create(service.getActivePromos()).assertNext(result -> {
            assertEquals("promo", result.getId());
            assertNull(result.getImage());
        }).verifyComplete();
    }

    @Test
    void failedSaveCleansUpNewImageAndKeepsOldFile() {
        request.setImage(image);
        when(repository.findById("promo")).thenReturn(Mono.just(promo));
        when(files.addFile(image)).thenReturn(Mono.just(uploaded));
        when(repository.save(any())).thenReturn(Mono.error(new RuntimeException("Save failed")));
        when(files.deleteFile("new-image")).thenReturn(Mono.empty());
        StepVerifier.create(service.updatePromoCodeById(request, "promo"))
                .expectErrorMessage("Save failed").verify();
        verify(files).deleteFile("new-image");
        verify(files, never()).deleteFile("old-image");
    }

    @Test
    void deletingPromotionDeletesItsImage() {
        when(repository.findById("promo")).thenReturn(Mono.just(promo));
        when(repository.delete(promo)).thenReturn(Mono.empty());
        when(files.deleteFile("old-image")).thenReturn(Mono.empty());
        StepVerifier.create(service.deletePromoCode("promo")).expectNextCount(1).verifyComplete();
        verify(files).deleteFile("old-image");
    }

    @Test
    void validatesRealPngAndJpeg() throws Exception {
        assertDoesNotThrow(() -> PromoImage.validate(image));
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "jpeg", bytes);
        image.setFileType("image/jpeg");
        image.setFileData(bytes.toByteArray());
        assertDoesNotThrow(() -> PromoImage.validate(image));
        assertDoesNotThrow(() -> PromoImage.validate(null));
    }

    @Test
    void rejectsOversizedUnsupportedCorruptAndMismatchedFiles() {
        image.setFileType("image/svg+xml");
        assertBadImage();
        image.setFileType("image/jpeg");
        assertBadImage(); // PNG bytes with a JPEG MIME type.
        image.setFileType("image/png");
        image.setFileData(new byte[PromoImage.MAX_BYTES + 1]);
        assertBadImage();
        image.setFileData(new byte[]{1, 2, 3});
        assertBadImage();
        image.setFileData(new byte[0]);
        assertBadImage();
    }

    @Test
    void rejectsInvalidImageBeforeUploadingOrSaving() {
        image.setFileData(new byte[]{1});
        request.setImage(image);
        StepVerifier.create(service.createPromo(request)).expectError(ResponseStatusException.class).verify();
        verifyNoInteractions(files, repository);
    }

    private void assertBadImage() {
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> PromoImage.validate(image)).getStatusCode());
    }
}
