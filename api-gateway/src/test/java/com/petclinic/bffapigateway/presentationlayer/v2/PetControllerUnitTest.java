package com.petclinic.bffapigateway.presentationlayer.v2;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class PetControllerUnitTest {

    @InjectMocks
    private PetController petController;

    @Mock
    private CustomersServiceClient customersServiceClient;

    private PetRequestDTO petRequestDTO;
    private PetResponseDTO petResponseDTO;
    private OwnerResponseDTO ownerResponseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up test data
        petRequestDTO = PetRequestDTO.builder()
                .ownerId("owner123")
                .name("Fluffy")
                .birthDate(new Date())
                .petTypeId("1")
                .isActive("true")
                .weight("5")
                .build();

        petResponseDTO = PetResponseDTO.builder()
                .petId("pet123")
                .ownerId("owner123")
                .name("Fluffy")
                .birthDate(new Date())
                .petTypeId("1")
                .isActive("true")
                .weight("5")
                .build();

        ownerResponseDTO = new OwnerResponseDTO();
        ownerResponseDTO.setOwnerId("owner123");
    }

    @Test
    void testUpdatePet_Success() {
        // Mock service layer methods
        when(customersServiceClient.updatePet(any(Mono.class), eq("pet123"))).thenReturn(Mono.just(petResponseDTO));
        when(customersServiceClient.getOwner("owner123", false)).thenReturn(Mono.just(ownerResponseDTO));
        when(customersServiceClient.updateOwner(eq("owner123"), any(Mono.class))).thenReturn(Mono.empty());

        Mono<ResponseEntity<PetResponseDTO>> result = petController.updatePet(Mono.just(petRequestDTO), "pet123");

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK && response.getBody().getPetId().equals("pet123"))
                .verifyComplete();
    }

    @Test
    void testUpdatePet_NotFound() {
        // Mock service layer methods for not found scenario
        when(customersServiceClient.updatePet(any(Mono.class), eq("pet123"))).thenReturn(Mono.empty());

        Mono<ResponseEntity<PetResponseDTO>> result = petController.updatePet(Mono.just(petRequestDTO), "pet123");

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();
    }

    @Test
    void testGetPetByPetId_Success() {
        // Mock service layer method
        when(customersServiceClient.getPetByPetId("pet123")).thenReturn(Mono.just(petResponseDTO));

        Mono<ResponseEntity<PetResponseDTO>> result = petController.getPetByPetId("pet123");

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK && response.getBody().getPetId().equals("pet123"))
                .verifyComplete();
    }

    @Test
    void testGetPetByPetId_NotFound() {
        // Mock service layer method for not found scenario
        when(customersServiceClient.getPetByPetId("pet123")).thenReturn(Mono.empty());

        Mono<ResponseEntity<PetResponseDTO>> result = petController.getPetByPetId("pet123");

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }

    @Test
    void whenAddPet_WithValidRequest_thenReturnCreatedPetResponseDTO() {
        PetRequestDTO newPetRequestDTO = PetRequestDTO.builder()
                .name("Buddy")
                .petTypeId("Dog")
                .ownerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .birthDate(new Date())
                .build();

        PetResponseDTO newPetResponseDTO = PetResponseDTO.builder()
                .petId("pet123")
                .name("Buddy")
                .petTypeId("Dog")
                .ownerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .birthDate(new Date())
                .build();

        when(customersServiceClient.addPet(any(Mono.class))).thenReturn(Mono.just(newPetResponseDTO));
        when(customersServiceClient.getOwner(anyString(), eq(false))).thenReturn(Mono.just(ownerResponseDTO));
        when(customersServiceClient.updateOwner(anyString(), any(Mono.class))).thenReturn(Mono.empty());

        Mono<ResponseEntity<PetResponseDTO>> result = petController.addPet(Mono.just(newPetRequestDTO));

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.CREATED && response.getBody().getPetId().equals("pet123"))
                .verifyComplete();
    }

    @Test
    void whenAddPet_WithInvalidOwner_thenReturnBadRequest() {
        PetRequestDTO newPetRequestDTO = PetRequestDTO.builder()
                .name("Buddy")
                .petTypeId("Dog")
                .ownerId("invalidOwnerId")
                .birthDate(new Date())
                .build();

        when(customersServiceClient.addPet(any(Mono.class))).thenReturn(Mono.empty());

        Mono<ResponseEntity<PetResponseDTO>> result = petController.addPet(Mono.just(newPetRequestDTO));

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();
    }

}