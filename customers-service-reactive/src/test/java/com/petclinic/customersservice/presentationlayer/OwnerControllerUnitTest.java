package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.OwnerService;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import com.petclinic.customersservice.domainclientlayer.FileResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OwnerControllerUnitTest {

    @Mock
    private OwnerService ownerService;

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

    @Test
    void getOwnerByOwnerId_ShouldReturnOwnerWithoutPhoto_WhenIncludePhotoFalse() {
        OwnerResponseDTO mockResponse = new OwnerResponseDTO();
        mockResponse.setOwnerId(TEST_OWNER_ID);
        mockResponse.setFirstName("John");

        doReturn(Mono.just(mockResponse)).when(ownerService).getOwnerByOwnerId(TEST_OWNER_ID, false);

        Mono<ResponseEntity<OwnerResponseDTO>> result = ownerController.getOwnerByOwnerId(TEST_OWNER_ID, false);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    OwnerResponseDTO body = response.getBody();
                    assertEquals(TEST_OWNER_ID, body.getOwnerId());
                    assertEquals("John", body.getFirstName());
                })
                .verifyComplete();
        verify(ownerService, times(1)).getOwnerByOwnerId(TEST_OWNER_ID, false);
    }

    @Test
    void getOwnerByOwnerId_ShouldReturnOwnerWithPhoto_WhenIncludePhotoTrue() {
        String imageData = "custom-image-data";
        OwnerResponseDTO mockResponse = new OwnerResponseDTO();
        mockResponse.setOwnerId(TEST_OWNER_ID);
        mockResponse.setFirstName("John");
        FileResponseDTO photo = FileResponseDTO.builder()
                .fileData(imageData)
                .fileType("image/jpeg")
                .build();
        mockResponse.setPhoto(photo);

        doReturn(Mono.just(mockResponse)).when(ownerService).getOwnerByOwnerId(TEST_OWNER_ID, true);

        Mono<ResponseEntity<OwnerResponseDTO>> result = ownerController.getOwnerByOwnerId(TEST_OWNER_ID, true);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    OwnerResponseDTO body = response.getBody();
                    assertEquals(TEST_OWNER_ID, body.getOwnerId());
                    assertEquals("John", body.getFirstName());
                    assertNotNull(body.getPhoto());
                    assertEquals(imageData, body.getPhoto().getFileData());
                    assertEquals("image/jpeg", body.getPhoto().getFileType());
                })
                .verifyComplete();
        verify(ownerService, times(1)).getOwnerByOwnerId(TEST_OWNER_ID, true);
    }

    @Test
    void getOwnerByOwnerId_ShouldReturnNotFound_WhenOwnerNotFound() {
        doReturn(Mono.empty()).when(ownerService).getOwnerByOwnerId(TEST_OWNER_ID, false);

        Mono<ResponseEntity<OwnerResponseDTO>> result = ownerController.getOwnerByOwnerId(TEST_OWNER_ID, false);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void updateOwnerPhoto_ShouldReturnUpdatedOwner() {
        com.petclinic.customersservice.domainclientlayer.FileRequestDTO photoRequest = 
            com.petclinic.customersservice.domainclientlayer.FileRequestDTO.builder()
                .fileName("profile-photo.jpg")
                .fileType("image/jpeg")
                .fileData("base64data")
                .build();

        OwnerResponseDTO mockResponse = new OwnerResponseDTO();
        mockResponse.setOwnerId(TEST_OWNER_ID);
        mockResponse.setFirstName("John");
        FileResponseDTO photo = FileResponseDTO.builder()
                .fileId("photo-456")
                .fileType("image/jpeg")
                .fileData("base64data")
                .build();
        mockResponse.setPhoto(photo);

        doReturn(Mono.just(mockResponse))
            .when(ownerService)
            .updateOwnerPhoto(org.mockito.ArgumentMatchers.eq(TEST_OWNER_ID), org.mockito.ArgumentMatchers.any(com.petclinic.customersservice.domainclientlayer.FileRequestDTO.class));

        Mono<ResponseEntity<OwnerResponseDTO>> result = ownerController.updateOwnerPhoto(TEST_OWNER_ID, photoRequest);

        StepVerifier.create(result)
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                OwnerResponseDTO body = response.getBody();
                if (body != null) {
                    assertEquals(TEST_OWNER_ID, body.getOwnerId());
                    assertNotNull(body.getPhoto());
                    assertEquals("photo-456", body.getPhoto().getFileId());
                }
            })
            .verifyComplete();
        verify(ownerService, times(1)).updateOwnerPhoto(org.mockito.ArgumentMatchers.eq(TEST_OWNER_ID), org.mockito.ArgumentMatchers.any(com.petclinic.customersservice.domainclientlayer.FileRequestDTO.class));
    }
}

