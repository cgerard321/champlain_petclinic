package com.petclinic.bffapigateway.presentationlayer.V1;


import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration"
})
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "logging.level.org.springframework.security=OFF"
})
class PetControllerV1Test {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CustomersServiceClient customersServiceClient;

    private final Date birthDate = new Date(20221010);

//    @Test
//    void shouldCreatePet() {
//        String ownerId = "ownerId-12345";
//
//        PetResponseDTO pet = new PetResponseDTO();
//        pet.setPetId("30-30-30-30");
//        pet.setOwnerId(ownerId);
//        pet.setName("Fluffy");
//        pet.setBirthDate(birthDate);
//        pet.setPetTypeId("5");
//        pet.setIsActive("true");
//
//        when(customersServiceClient.createPetForOwner(eq(ownerId), any(PetRequestDTO.class)))
//                .thenReturn(Mono.just(pet));
//
//        PetRequestDTO petRequest = new PetRequestDTO();
//        petRequest.setOwnerId(ownerId);
//        petRequest.setName("Fluffy");
//        petRequest.setBirthDate(birthDate);
//        petRequest.setPetTypeId("5");
//        petRequest.setIsActive("true");
//
//        webTestClient.post()
//                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
//                .bodyValue(petRequest)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody()
//                .jsonPath("$.petId").isEqualTo("30-30-30-30")
//                .jsonPath("$.name").isEqualTo("Fluffy");
//
//        verify(customersServiceClient, times(1))
//                .createPetForOwner(eq(ownerId), any(PetRequestDTO.class));
//    }
//
//    @Test
//    void shouldDeletePet() {
//        String petId = "petId-123";
//        String ownerId = "ownerId-1";
//
//        when(customersServiceClient.deletePetByPetId(petId))
//                .thenReturn(Mono.empty());
//
//        webTestClient.delete()
//                .uri("/api/gateway/owners/{ownerId}/pets/{petId}", ownerId, petId)
//                .exchange()
//                .expectStatus().isNoContent();
//
//        verify(customersServiceClient, times(1))
//                .deletePetByPetId(petId);
//    }
//
//    @Test
//    void shouldPatchPet() {
//        String petId = "petId-123";
//        String ownerId = "ownerId-1";
//
//        PetRequestDTO petRequestDTO = new PetRequestDTO();
//        petRequestDTO.setPetId(petId);
//        petRequestDTO.setIsActive("true");
//
//        PetResponseDTO expectedPetResponse = new PetResponseDTO();
//        expectedPetResponse.setPetId(petId);
//        expectedPetResponse.setIsActive("true");
//
//        when(customersServiceClient.patchPet(any(PetRequestDTO.class), eq(petId)))
//                .thenReturn(Mono.just(expectedPetResponse));
//
//        webTestClient.patch()
//                .uri("/api/gateway/owners/{ownerId}/pets/{petId}", ownerId, petId)
//                .bodyValue(petRequestDTO)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.petId").isEqualTo(petId)
//                .jsonPath("$.isActive").isEqualTo("true");
//
//        verify(customersServiceClient, times(1))
//                .patchPet(any(PetRequestDTO.class), eq(petId));
//    }

    @Test
    void ifOwnerIdIsNotSpecifiedInUrlThrowNotAllowed() {
        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("Fluffy");
        petRequest.setBirthDate(birthDate);
        petRequest.setPetTypeId("5");
        petRequest.setIsActive("true");

        webTestClient.post()
                .uri("/api/gateway/owners/pets") // Missing ownerId
                .bodyValue(petRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND); // This endpoint doesn't exist

        verifyNoInteractions(customersServiceClient);
    }

    @Test
    void shouldThrowNotFoundWhenOwnerIdIsNotSpecifiedOnDeletePets() {
        String petId = "petId-123";

        webTestClient.delete()
                .uri("/api/gateway/owners/pets/{petId}", petId) // Missing ownerId in path
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verifyNoInteractions(customersServiceClient);
    }

    @Test
    void shouldThrowMethodNotAllowedWhenDeletePetsIsMissingPetId() {
        String ownerId = "ownerId-20";

        webTestClient.delete()
                .uri("/api/gateway/owners/{ownerId}/pets", ownerId) // Missing petId
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND); // This endpoint pattern doesn't exist

        verifyNoInteractions(customersServiceClient);
    }

    @Test
    void shouldHandleInvalidJsonInCreateRequest() {
        String ownerId = "ownerId-12345";
        String invalidJson = "{\"name\": }"; // Invalid JSON

        webTestClient.post()
                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().is4xxClientError();

        verify(customersServiceClient, never()).createPetForOwner(any(), any());
    }

    @Test
    void shouldHandleEmptyRequestBodyOnCreate() {
        String ownerId = "ownerId-12345";

        webTestClient.post()
                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();

        verify(customersServiceClient, never()).createPetForOwner(any(), any());
    }

    @Test
    void shouldHandleInvalidJsonInPatchRequest() {
        String ownerId = "ownerId-1";
        String petId = "petId-123";
        String invalidJson = "{\"isActive\": }"; // Invalid JSON

        webTestClient.patch()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().is4xxClientError();

        verify(customersServiceClient, never()).patchPet(any(), any());
    }

    @Test
    void shouldHandleEmptyRequestBodyOnPatch() {
        String ownerId = "ownerId-1";
        String petId = "petId-123";

        webTestClient.patch()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();

        verify(customersServiceClient, never()).patchPet(any(), any());
    }
}