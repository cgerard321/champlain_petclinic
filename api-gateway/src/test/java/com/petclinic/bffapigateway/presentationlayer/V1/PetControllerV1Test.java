package com.petclinic.bffapigateway.presentationlayer.V1;

import com.petclinic.bffapigateway.presentationlayer.v1.PetControllerV1;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
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

@RunWith(SpringRunner.class)
@WebFluxTest(
        controllers = {PetControllerV1.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtTokenFilter.class, RoleFilter.class, IsUserFilter.class}
        )
)
@AutoConfigureWebTestClient
class PetControllerV1Test {

    @Autowired
    private WebTestClient client;

    @MockBean
    private CustomersServiceClient customersServiceClient;

    private final Date birthDate = new Date(20221010);

//    @Test
//    void shouldCreatePet() {
//        String ownerId = "ownerId-12345";
//
//        PetResponseDTO pet = new PetResponseDTO();
//        pet.setPetId("");
//        pet.setOwnerId(ownerId);
//        pet.setName("Fluffy");
//        pet.setBirthDate(birthDate);
//        pet.setPetTypeId("5");
//        pet.setWeight("10.5");
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
//        pet.setWeight("10.5");
//        petRequest.setIsActive("true");
//
//        client.post()
//                .uri("/api/gateway/owners/pets")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromValue(petRequest))
//                .exchange()
//                .expectStatus().isCreated()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(PetResponseDTO.class)
//                .value(response -> {
//                    assertEquals("30-30-30-30", response.getPetId());
//                    assertEquals("Fluffy", response.getName());
//                    assertEquals(ownerId, response.getOwnerId());
//                    assertEquals(birthDate, response.getBirthDate());
//                    assertEquals("5", response.getPetTypeId());
//                    assertEquals("10.5", response.getWeight());
//                    assertEquals("true", response.getIsActive());
//                });
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
//        client.delete()
//                .uri("/api/gateway/owners/" + ownerId + "/pets/" + petId)
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
//        client.patch()
//                .uri("/api/gateway/owners/" + ownerId + "/pets/" + petId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromValue(petRequestDTO))
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(PetResponseDTO.class)
//                .value(response -> {
//                    assertEquals(petId, response.getPetId());
//                    assertEquals("true", response.getIsActive());
//                });
//
//        verify(customersServiceClient, times(1))
//                .patchPet(any(PetRequestDTO.class), eq(petId));
//    }



    @Test
    void whenDeletePetType_ReturnsWebClientError_ShouldReturnStatusNotFound() {
        try {
            String petTypeId = "4283c9b8-4ffd-4866-a5ed-287117c60a40";
            WebClientResponseException serviceException = WebClientResponseException.create(
                    404, "Not Found", null, null, null);

            when(customersServiceClient.deletePetTypeV2(petTypeId))
                    .thenReturn(Mono.error(serviceException));

            client.delete()
                    .uri("/api/gateway/owners/petTypes/{petTypeId}", petTypeId)
                    .exchange()
                    .expectStatus().is4xxClientError();

        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }



//    @Test
//    void deletePetType_WhenServiceTimeout_ShouldReturnGatewayTimeout() {
//        try {
//            // Given
//            String petTypeId = "4283c9b8-4ffd-4866-a5ed-287117c60a40";
//            when(customersServiceClient.deletePetTypeV2(petTypeId))
//                    .thenReturn(Mono.error(new RuntimeException("Connection timeout")));
//
//            // When & Then
//            client.delete()
//                    .uri("/api/gateway/owners/petTypes/{petTypeId}", petTypeId)
//                    .exchange()
//                    .expectStatus().is5xxServerError();
//
//        } catch (Exception e) {
//            System.err.println("Test failed with exception: " + e.getMessage());
//            e.printStackTrace();
//            fail("Test failed: " + e.getMessage());
//        }
//    }


    @Test
    void ifOwnerIdIsNotSpecifiedInUrlThrowNotAllowed() {
        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("Fluffy");
        petRequest.setBirthDate(birthDate);
        petRequest.setPetTypeId("5");
        petRequest.setIsActive("true");

        client.post()
                .uri("/api/gateway/owners/pets")
                .bodyValue(petRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);

        verifyNoInteractions(customersServiceClient);
    }

    @Test
    void shouldThrowNotFoundWhenOwnerIdIsNotSpecifiedOnDeletePets() {
        String petId = "petId-123";

        client.delete()
                .uri("/api/gateway/owners/pets/{petId}", petId) // Missing ownerId in path
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verifyNoInteractions(customersServiceClient);
    }

    @Test
    void shouldThrowMethodNotAllowedWhenDeletePetsIsMissingPetId() {
        String ownerId = "ownerId-20";

        client.delete()
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

        client.post()
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

        client.post()
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

        client.patch()
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

        client.patch()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();

        verify(customersServiceClient, never()).patchPet(any(), any());
    }

    @Test
    void updatePetType_shouldSucceed() {
        String petTypeId = "petTypeId-123";
        PetTypeRequestDTO requestDTO = new PetTypeRequestDTO();
        requestDTO.setName("Updated Dog");
        requestDTO.setPetTypeDescription("Updated Mammal");

        PetTypeResponseDTO responseDTO = new PetTypeResponseDTO();
        responseDTO.setPetTypeId(petTypeId);
        responseDTO.setName("Updated Dog");
        responseDTO.setPetTypeDescription("Updated Mammal");

        when(customersServiceClient.updatePetType(eq(petTypeId), any(Mono.class)))
                .thenReturn(Mono.just(responseDTO));

        client.put()
                .uri("/api/gateway/owners/petTypes/{petTypeId}", petTypeId)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petTypeId").isEqualTo(petTypeId)
                .jsonPath("$.name").isEqualTo("Updated Dog")
                .jsonPath("$.petTypeDescription").isEqualTo("Updated Mammal");

        verify(customersServiceClient, times(1)).updatePetType(eq(petTypeId), any(Mono.class));
    }
}