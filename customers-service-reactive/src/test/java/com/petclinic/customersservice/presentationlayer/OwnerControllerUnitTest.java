package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.OwnerService;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.domainclientlayer.FileResponseDTO;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OwnerControllerUnitTest {

    @Mock
    private OwnerService ownerService;

    @Mock
    private FilesServiceClient filesServiceClient;

    @InjectMocks
    private OwnerController ownerController;

    private final String TEST_OWNER_ID = "ownerId-123";
    private Owner mockOwner;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();

        mockOwner = new Owner();
        mockOwner.setOwnerId(TEST_OWNER_ID);
        mockOwner.setFirstName("John");
        mockOwner.setPhotoId("photo-123");
    }

    //@Test
    void getOwnerPhoto_ShouldReturnCustomPhoto_WhenFound() {
        byte[] imageBytes = "custom-image-data".getBytes();

        FileResponseDTO fileDTO = new FileResponseDTO();

        doReturn(Mono.just(mockOwner)).when(ownerService).getOwnerEntityByOwnerId(TEST_OWNER_ID);
        doReturn(Mono.just(fileDTO)).when(filesServiceClient).getFile("photo-123");

        Mono<ResponseEntity<byte[]>> result = ownerController.getOwnerPhoto(TEST_OWNER_ID);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
                    assertArrayEquals(imageBytes, response.getBody());
                })
                .verifyComplete();
        verify(filesServiceClient, times(1)).getFile("photo-123");
    }

    //@Test
    void getOwnerPhoto_ShouldReturnDefaultPhoto_WhenOwnerHasNoPhotoId() {
        byte[] defaultBytes = new byte[0];

        Owner ownerWithoutPhoto = new Owner();
        ownerWithoutPhoto.setOwnerId(TEST_OWNER_ID);
        ownerWithoutPhoto.setFirstName("John");
        ownerWithoutPhoto.setPhotoId(null);

        FileResponseDTO defaultFileDTO = new FileResponseDTO();

        doReturn(Mono.just(ownerWithoutPhoto)).when(ownerService).getOwnerEntityByOwnerId(TEST_OWNER_ID);
        doReturn(Mono.just(defaultFileDTO)).when(filesServiceClient).getFile("defaultProfilePicture.png");

        Mono<ResponseEntity<byte[]>> result = ownerController.getOwnerPhoto(TEST_OWNER_ID);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
                    assertArrayEquals(defaultBytes, response.getBody());
                })
                .verifyComplete();
        verify(filesServiceClient, times(1)).getFile("defaultProfilePicture.png");
        verify(filesServiceClient, never()).getFile("photo-123");
    }

    @Test
    void getOwnerPhoto_ShouldFallbackToDefault_WhenFileServiceFails() {
        doReturn(Mono.just(mockOwner)).when(ownerService).getOwnerEntityByOwnerId(TEST_OWNER_ID);
        doReturn(Mono.error(new RuntimeException("External Service Down"))).when(filesServiceClient).getFile(anyString());

        Mono<ResponseEntity<byte[]>> result = ownerController.getOwnerPhoto(TEST_OWNER_ID);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
                    assertArrayEquals(new byte[0], response.getBody());
                })
                .verifyComplete();
        verify(filesServiceClient, times(1)).getFile("photo-123");
    }

    @Test
    void getOwnerPhoto_ShouldFallbackToDefault_WhenOwnerNotFound() {
        doReturn(Mono.empty()).when(ownerService).getOwnerEntityByOwnerId(TEST_OWNER_ID);

        Mono<ResponseEntity<byte[]>> result = ownerController.getOwnerPhoto(TEST_OWNER_ID);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
                    assertArrayEquals(new byte[0], response.getBody());
                })
                .verifyComplete();
        verify(filesServiceClient, never()).getFile(anyString());
    }

    //@Test
    void getOwnerPhoto_ShouldFallbackToDefault_WhenFileDataIsNull() {
        FileResponseDTO fileDTO = new FileResponseDTO();

        doReturn(Mono.just(mockOwner)).when(ownerService).getOwnerEntityByOwnerId(TEST_OWNER_ID);
        doReturn(Mono.just(fileDTO)).when(filesServiceClient).getFile(anyString());

        Mono<ResponseEntity<byte[]>> result = ownerController.getOwnerPhoto(TEST_OWNER_ID);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
                    assertArrayEquals(new byte[0], response.getBody());
                })
                .verifyComplete();
    }

    //@Test
    void getOwnerPhoto_ShouldInferContentTypeFromFilename_PNG() {
        byte[] imageBytes = "png-data".getBytes();
        FileResponseDTO fileDTO = new FileResponseDTO();

        doReturn(Mono.just(mockOwner)).when(ownerService).getOwnerEntityByOwnerId(TEST_OWNER_ID);
        doReturn(Mono.just(fileDTO)).when(filesServiceClient).getFile(anyString());

        Mono<ResponseEntity<byte[]>> result = ownerController.getOwnerPhoto(TEST_OWNER_ID);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
                    assertArrayEquals(imageBytes, response.getBody());
                })
                .verifyComplete();
    }

   // @Test
    void getOwnerPhoto_ShouldInferContentTypeFromFilename_JPEG() {
        byte[] imageBytes = "jpeg-data".getBytes();
        FileResponseDTO fileDTO = new FileResponseDTO();

        doReturn(Mono.just(mockOwner)).when(ownerService).getOwnerEntityByOwnerId(TEST_OWNER_ID);
        doReturn(Mono.just(fileDTO)).when(filesServiceClient).getFile(anyString());

        Mono<ResponseEntity<byte[]>> result = ownerController.getOwnerPhoto(TEST_OWNER_ID);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
                    assertArrayEquals(imageBytes, response.getBody());
                })
                .verifyComplete();
    }

    //@Test
    void getOwnerPhoto_ShouldDefaultToOctetStream_WhenUnknownContentTypeAndFilename() {
        byte[] imageBytes = "raw-data".getBytes();
        FileResponseDTO fileDTO = new FileResponseDTO();

        doReturn(Mono.just(mockOwner)).when(ownerService).getOwnerEntityByOwnerId(TEST_OWNER_ID);
        doReturn(Mono.just(fileDTO)).when(filesServiceClient).getFile(anyString());

        Mono<ResponseEntity<byte[]>> result = ownerController.getOwnerPhoto(TEST_OWNER_ID);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
                    assertArrayEquals(imageBytes, response.getBody());
                })
                .verifyComplete();
    }
}
