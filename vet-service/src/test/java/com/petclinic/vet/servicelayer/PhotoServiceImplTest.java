package com.petclinic.vet.servicelayer;

import com.petclinic.vet.businesslayer.photos.PhotoService;
import com.petclinic.vet.dataaccesslayer.photos.Photo;
import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.vets.Vet;
import com.petclinic.vet.dataaccesslayer.vets.VetRepository;
import com.petclinic.vet.domainclientlayer.FilesServiceClient;
import com.petclinic.vet.presentationlayer.photos.PhotoRequestDTO;
import com.petclinic.vet.presentationlayer.photos.PhotoResponseDTO;
import com.petclinic.vet.utils.exceptions.InvalidInputException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class PhotoServiceImplTest {
    @Autowired
    PhotoService photoService;

    @MockBean
    PhotoRepository photoRepository;
    @MockBean
    VetRepository vetRepository;
    @MockBean
    FilesServiceClient filesServiceClient;

    //To counter missing bean error
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

    String VET_ID = "6748786";
    byte[] photoData = {123, 23, 75, 34};
    Photo photo = Photo.builder()
            .vetId(VET_ID)
            .filename("vet_default.jpg")
            .imgType("image/jpeg")
            .data(photoData)
            .build();

    @Test
    void getPhotoByValidVetId() {
        // Create a vet with null imageId to trigger fallback to MongoDB
        Vet vet = Vet.builder()
                .vetId(VET_ID)
                .imageId(null)
                .build();
        
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(photoRepository.findByVetId(anyString())).thenReturn(Mono.just(photo));

        Mono<PhotoResponseDTO> photoMono = photoService.getPhotoByVetId(VET_ID);

        StepVerifier
                .create(photoMono)
                .consumeNextWith(photoResponse -> {
                    assertNotNull(photoResponse);
                    assertEquals(VET_ID, photoResponse.getVetId());
                    assertEquals("vet_default.jpg", photoResponse.getFilename());
                    assertEquals("image/jpeg", photoResponse.getImgType());
                })
                .verifyComplete();
    }
    @Test
    void getDefaultPhotoByValidVetId() {
        String photoName = "vet_default.jpg";
        Photo savedDefaultPhoto = new Photo();
        savedDefaultPhoto.setVetId(VET_ID);
        savedDefaultPhoto.setFilename(photoName);
        savedDefaultPhoto.setImgType("image/jpeg");
        savedDefaultPhoto.setData(photoData);
        when(photoRepository.save(any(Photo.class))).thenReturn(Mono.just(savedDefaultPhoto));

        when(photoRepository.findByVetId(anyString())).thenReturn(Mono.just(photo));

        Mono<PhotoResponseDTO> defaultPhotoMono = photoService.getDefaultPhotoByVetId(VET_ID);

        StepVerifier
                .create(defaultPhotoMono)
                .consumeNextWith(image -> {
                    assertNotNull(image);

                    PhotoResponseDTO photo = defaultPhotoMono.block();
                    assertEquals(photo.getVetId(), image.getVetId());
                })
                .verifyComplete();
    }


    @Test
    void insertPhotoOfVet_savesAndReturnsDTO() throws Exception {
        String vetId = "VET_ID";
        String photoName = "vet_default.jpg";
        byte[] photoData = "fake-bytes".getBytes();

        PhotoRequestDTO photoRequest = PhotoRequestDTO.builder()
                .vetId(vetId)
                .filename(photoName)
                .imgType("image/jpeg")
                .data(photoData)
                .build();

        Photo savedPhoto = Photo.builder()
                .vetId(vetId)
                .filename(photoName)
                .imgType("image/jpeg")
                .data(photoData)
                .build();

        when(photoRepository.save(ArgumentMatchers.any(Photo.class)))
                .thenReturn(Mono.just(savedPhoto));

        Mono<PhotoResponseDTO> result = photoService.insertPhotoOfVet(vetId, Mono.just(photoRequest));

        StepVerifier.create(result)
                .assertNext(res -> {
                    assertNotNull(res);
                    assertEquals(vetId, res.getVetId());
                    assertEquals(photoName, res.getFilename());
                    assertEquals("image/jpeg", res.getImgType());
                })
                .verifyComplete();

        verify(photoRepository, times(1)).save(any(Photo.class));
    }

    @Test
    void updatePhotoOfVet() {
        String photoName = "vet_default.jpg";
        PhotoRequestDTO photoRequest = PhotoRequestDTO.builder()
                .vetId(VET_ID)
                .filename(photoName)
                .imgType("image/jpeg")
                .data(photoData)
                .build();
                
        Photo savedPhoto = new Photo();
        savedPhoto.setVetId(VET_ID);
        savedPhoto.setFilename(photoName);
        savedPhoto.setImgType("image/jpeg");
        savedPhoto.setData(photoData);

        when(photoRepository.save(any(Photo.class))).thenReturn(Mono.just(savedPhoto));
        when(photoRepository.findByVetId(anyString())).thenReturn(Mono.just(savedPhoto));

        Mono<PhotoResponseDTO> savedPhotoMono = photoService.updatePhotoByVetId(VET_ID, Mono.just(photoRequest));

        StepVerifier
                .create(savedPhotoMono)
                .consumeNextWith(photoResponse -> {
                    assertNotNull(photoResponse);
                    assertEquals(VET_ID, photoResponse.getVetId());
                    assertEquals(photoName, photoResponse.getFilename());
                    assertEquals("image/jpeg", photoResponse.getImgType());
                })
                .verifyComplete();
    }

    @Test
    void deletePhotoByVetId_PhotoExists_Success() {
        // Arrange
        String vetId = VET_ID;

        // Mock photoRepository.findByVetId(vetId) to return Mono.just(photo)
        when(photoRepository.findByVetId(vetId)).thenReturn(Mono.just(photo));

        // Mock photoRepository.deleteByVetId(vetId) to return Mono.just(1)
        when(photoRepository.deleteByVetId(vetId)).thenReturn(Mono.just(1)); // Assuming 1 row deleted

        // Mock photoRepository.save(...) in insertDefaultPhoto
        when(photoRepository.save(any(Photo.class))).thenReturn(Mono.just(photo));

        // Act
        Mono<Void> result = photoService.deletePhotoByVetId(vetId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(photoRepository, times(1)).findByVetId(vetId);
        verify(photoRepository, times(1)).deleteByVetId(vetId);
        verify(photoRepository, times(1)).save(any(Photo.class));
    }

    @Test
    void deletePhotoByVetId_PhotoDoesNotExist_ThrowsException() {
        // Arrange
        String vetId = VET_ID;

        // Mock photoRepository.findByVetId(vetId) to return Mono.empty()
        when(photoRepository.findByVetId(vetId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = photoService.deletePhotoByVetId(vetId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidInputException &&
                        throwable.getMessage().equals("Photo not found for vetId: " + vetId))
                .verify();

        verify(photoRepository, times(1)).findByVetId(vetId);
        verify(photoRepository, never()).deleteByVetId(anyString());
        verify(photoRepository, never()).save(any(Photo.class));
        verify(photoRepository, never()).deleteByVetId(anyString());
        verify(photoRepository, never()).save(any(Photo.class));
    }

}