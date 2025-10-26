package com.petclinic.bffapigateway.presentationlayer.v1.Owners;

import com.petclinic.bffapigateway.presentationlayer.v1.PetControllerV1;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;

@RunWith(SpringRunner.class)
@WebFluxTest(
        controllers = {PetControllerV1.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtTokenFilter.class, RoleFilter.class, IsUserFilter.class}
        )
)
@AutoConfigureWebTestClient
class PetsControllerV1UnitTests {

    @Autowired
    private WebTestClient client;

    @MockBean
    private CustomersServiceClient customersServiceClient;

    private final Date birthDate = new Date(20221010);

    @Test
    void whenUpdatePet_thenReturnUpdatedPet() {
        String petId = "petId-123";

        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("Updated Fluffy");
        petRequest.setBirthDate(birthDate);
        petRequest.setPetTypeId("5");
        petRequest.setWeight("12.5");
        petRequest.setIsActive("true");

        PetResponseDTO expectedResponse = new PetResponseDTO();
        expectedResponse.setPetId(petId);
        expectedResponse.setName("Updated Fluffy");
        expectedResponse.setBirthDate(birthDate);
        expectedResponse.setPetTypeId("5");
        expectedResponse.setWeight("12.5");
        expectedResponse.setIsActive("true");

        when(customersServiceClient.updatePet(any(Mono.class), eq(petId)))
                .thenReturn(Mono.just(expectedResponse));

        client.put()
                .uri("/api/gateway/pets/{petId}", petId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(petRequest))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PetResponseDTO.class)
                .value(response -> {
                    assertEquals(petId, response.getPetId());
                    assertEquals("Updated Fluffy", response.getName());
                    assertEquals("12.5", response.getWeight());
                    assertEquals("true", response.getIsActive());
                });

        verify(customersServiceClient, times(1))
                .updatePet(any(Mono.class), eq(petId));
    }

    @Test
    void whenGetPetByPetId_thenReturnPet() {
        String ownerId = "valid-owner-id";
        String petId = "petId-123";
        boolean includePhoto = false;

        PetResponseDTO expectedResponse = new PetResponseDTO();
        expectedResponse.setPetId(petId);
        expectedResponse.setName("Fluffy");
        expectedResponse.setBirthDate(birthDate);
        expectedResponse.setPetTypeId("5");
        expectedResponse.setWeight("10.5");
        expectedResponse.setIsActive("true");
        expectedResponse.setOwnerId(ownerId);

        when(customersServiceClient.getPetByPetId(petId, includePhoto))
                .thenReturn(Mono.just(expectedResponse));

        client.get()
                .uri("/api/gateway/pets/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PetResponseDTO.class)
                .value(response -> {
                    assertEquals(petId, response.getPetId());
                    assertEquals("Fluffy", response.getName());
                    assertEquals("10.5", response.getWeight());
                });

        verify(customersServiceClient, times(1))
                .getPetByPetId(petId, includePhoto);
    }

    @Test
    void whenDeletePet_thenReturnNoContent() {
        String petId = "petId-123";

        when(customersServiceClient.deletePetByPetId(petId))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/pets/{petId}", petId)
                .exchange()
                .expectStatus().isNoContent();

        verify(customersServiceClient, times(1))
                .deletePetByPetId(petId);
    }

    @Test
    void whenUpdatePetForOwner_thenReturnUpdatedPet() {
        String petId = "petId-123";
        String ownerId = "ownerId-456";

        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("Updated Fluffy");
        petRequest.setBirthDate(birthDate);
        petRequest.setPetTypeId("5");
        petRequest.setWeight("12.5");
        petRequest.setIsActive("true");

        PetResponseDTO expectedResponse = new PetResponseDTO();
        expectedResponse.setPetId(petId);
        expectedResponse.setName("Updated Fluffy");
        expectedResponse.setBirthDate(birthDate);
        expectedResponse.setPetTypeId("5");
        expectedResponse.setWeight("12.5");
        expectedResponse.setIsActive("true");

        when(customersServiceClient.updatePet(any(Mono.class), eq(petId)))
                .thenReturn(Mono.just(expectedResponse));

        client.put()
                .uri("/api/gateway/pets/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(petRequest))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PetResponseDTO.class)
                .value(response -> {
                    assertEquals(petId, response.getPetId());
                    assertEquals("Updated Fluffy", response.getName());
                    assertEquals("12.5", response.getWeight());
                });

        verify(customersServiceClient, times(1))
                .updatePet(any(Mono.class), eq(petId));
    }

    @Test
    void whenGetPetForOwner_thenReturnPet() {
        String petId = "petId-123";
        String ownerId = "ownerId-456";

        PetResponseDTO expectedResponse = new PetResponseDTO();
        expectedResponse.setPetId(petId);
        expectedResponse.setName("Fluffy");
        expectedResponse.setBirthDate(birthDate);
        expectedResponse.setPetTypeId("5");
        expectedResponse.setWeight("10.5");
        expectedResponse.setIsActive("true");

        when(customersServiceClient.getPetByPetId(petId, false))
                .thenReturn(Mono.just(expectedResponse));

        client.get()
                .uri("/api/gateway/pets/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PetResponseDTO.class)
                .value(response -> {
                    assertEquals(petId, response.getPetId());
                    assertEquals("Fluffy", response.getName());
                    assertEquals("10.5", response.getWeight());
                });

        verify(customersServiceClient, times(1))
                .getPetByPetId(petId, false);
    }

    @Test
    void whenCreatePet_withoutOwnerId_thenReturnNotFound() {
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
    void whenDeletePet_withoutOwnerId_thenReturnNotFound() {
        String petId = "petId-123";

        client.delete()
                .uri("/api/gateway/owners/pets/{petId}", petId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verifyNoInteractions(customersServiceClient);
    }

    @Test
    void whenDeletePet_withoutPetId_thenReturnNotFound() {
        String ownerId = "ownerId-20";

        client.delete()
                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);

        verifyNoInteractions(customersServiceClient);
    }

    @Test
    void whenCreatePet_withInvalidJson_thenReturnBadRequest() {
        String ownerId = "ownerId-12345";
        String invalidJson = "{\"name\": }";

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().is4xxClientError();

        verify(customersServiceClient, never()).createPetForOwner(any(), any());
    }

    @Test
    void whenCreatePet_withEmptyBody_thenReturnBadRequest() {
        String ownerId = "ownerId-12345";

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();

        verify(customersServiceClient, never()).createPetForOwner(any(), any());
    }

    @Test
    void whenDeletePetPhoto_thenReturnOk() {
        String petId = "petId-123";

        PetResponseDTO expectedResponse = new PetResponseDTO();
        expectedResponse.setPetId(petId);
        expectedResponse.setName("Fluffy");
        expectedResponse.setBirthDate(birthDate);
        expectedResponse.setPetTypeId("5");
        expectedResponse.setWeight("10.5");
        expectedResponse.setIsActive("true");
        expectedResponse.setPhoto(null);

        when(customersServiceClient.deletePetPhoto(petId))
                .thenReturn(Mono.just(expectedResponse));

        client.patch()
                .uri("/api/gateway/pets/{petId}/photo", petId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PetResponseDTO.class)
                .value(response -> {
                    assertEquals(petId, response.getPetId());
                    assertEquals("Fluffy", response.getName());
                    assertEquals("10.5", response.getWeight());
                    assertNull(response.getPhoto());
                });

        verify(customersServiceClient, times(1))
                .deletePetPhoto(petId);
    }

    @Test
    void whenDeletePetPhoto_withNonExistentPet_thenReturnNotFound() {
        String petId = "non-existent-pet-id";

        when(customersServiceClient.deletePetPhoto(petId))
                .thenReturn(Mono.empty());

        client.patch()
                .uri("/api/gateway/pets/{petId}/photo", petId)
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1))
                .deletePetPhoto(petId);
    }

    @Test
    void whenDeletePetPhoto_withInvalidPetId_thenReturnNotFound() {
        String invalidPetId = "invalid-id";

        when(customersServiceClient.deletePetPhoto(invalidPetId))
                .thenReturn(Mono.empty());

        client.patch()
                .uri("/api/gateway/pets/{petId}/photo", invalidPetId)
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1))
                .deletePetPhoto(invalidPetId);
    }

    @Test
    void whenAddPet_thenReturnCreatedPet() {
        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("New Pet");
        petRequest.setBirthDate(birthDate);
        petRequest.setPetTypeId("5");
        petRequest.setWeight("8.5");
        petRequest.setIsActive("true");

        PetResponseDTO expectedResponse = new PetResponseDTO();
        expectedResponse.setPetId("new-pet-id");
        expectedResponse.setName("New Pet");
        expectedResponse.setBirthDate(birthDate);
        expectedResponse.setPetTypeId("5");
        expectedResponse.setWeight("8.5");
        expectedResponse.setIsActive("true");

        when(customersServiceClient.addPet(any(Mono.class)))
                .thenReturn(Mono.just(expectedResponse));

        client.post()
                .uri("/api/gateway/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(petRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PetResponseDTO.class)
                .value(response -> {
                    assertEquals("new-pet-id", response.getPetId());
                    assertEquals("New Pet", response.getName());
                    assertEquals("8.5", response.getWeight());
                    assertEquals("true", response.getIsActive());
                });

        verify(customersServiceClient, times(1))
                .addPet(any(Mono.class));
    }

    @Test
    void whenAddPet_withEmptyResponse_thenReturnBadRequest() {
        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("New Pet");
        petRequest.setBirthDate(birthDate);
        petRequest.setPetTypeId("5");
        petRequest.setWeight("8.5");
        petRequest.setIsActive("true");

        when(customersServiceClient.addPet(any(Mono.class)))
                .thenReturn(Mono.empty());

        client.post()
                .uri("/api/gateway/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(petRequest))
                .exchange()
                .expectStatus().isBadRequest();

        verify(customersServiceClient, times(1))
                .addPet(any(Mono.class));
    }

    @Test
    void whenGetPetByPetId_withIncludePhotoTrue_thenReturnPetWithPhoto() {
        String petId = "petId-123";
        boolean includePhoto = true;

        PetResponseDTO expectedResponse = new PetResponseDTO();
        expectedResponse.setPetId(petId);
        expectedResponse.setName("Fluffy");
        expectedResponse.setBirthDate(birthDate);
        expectedResponse.setPetTypeId("5");
        expectedResponse.setWeight("10.5");
        expectedResponse.setIsActive("true");

        when(customersServiceClient.getPetByPetId(petId, includePhoto))
                .thenReturn(Mono.just(expectedResponse));

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/gateway/pets/{petId}")
                        .queryParam("includePhoto", includePhoto)
                        .build(petId))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PetResponseDTO.class)
                .value(response -> {
                    assertEquals(petId, response.getPetId());
                    assertEquals("Fluffy", response.getName());
                    assertEquals("10.5", response.getWeight());
                });

        verify(customersServiceClient, times(1))
                .getPetByPetId(petId, includePhoto);
    }

    @Test
    void whenGetPetByPetId_withNonExistentPet_thenReturnNotFound() {
        String nonExistentPetId = "non-existent-pet-id";

        when(customersServiceClient.getPetByPetId(nonExistentPetId, false))
                .thenReturn(Mono.empty());

        client.get()
                .uri("/api/gateway/pets/{petId}", nonExistentPetId)
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1))
                .getPetByPetId(nonExistentPetId, false);
    }

    @Test
    void whenGetAllPets_thenReturnPets() {
        PetResponseDTO pet1 = new PetResponseDTO();
        pet1.setPetId("pet-1");
        pet1.setName("Fluffy");
        pet1.setBirthDate(birthDate);
        pet1.setPetTypeId("5");
        pet1.setWeight("10.5");
        pet1.setIsActive("true");

        PetResponseDTO pet2 = new PetResponseDTO();
        pet2.setPetId("pet-2");
        pet2.setName("Buddy");
        pet2.setBirthDate(birthDate);
        pet2.setPetTypeId("2");
        pet2.setWeight("15.0");
        pet2.setIsActive("true");

        when(customersServiceClient.getAllPets())
                .thenReturn(reactor.core.publisher.Flux.just(pet1, pet2));

        client.get()
                .uri("/api/gateway/pets")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.valueOf("text/event-stream;charset=UTF-8"));

        verify(customersServiceClient, times(1))
                .getAllPets();
    }

    @Test
    void whenPatchPet_withValidData_thenReturnUpdatedPet() {
        String petId = "petId-123";
        String isActive = "false";

        PetResponseDTO expectedResponse = new PetResponseDTO();
        expectedResponse.setPetId(petId);
        expectedResponse.setName("Fluffy");
        expectedResponse.setBirthDate(birthDate);
        expectedResponse.setPetTypeId("5");
        expectedResponse.setWeight("10.5");
        expectedResponse.setIsActive(isActive);

        when(customersServiceClient.patchPet(isActive, petId))
                .thenReturn(Mono.just(expectedResponse));

        client.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/gateway/pets/{petId}/active")
                        .queryParam("isActive", isActive)
                        .build(petId))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.valueOf("application/json"))
                .expectBody(PetResponseDTO.class)
                .value(response -> {
                    assertEquals(petId, response.getPetId());
                    assertEquals("Fluffy", response.getName());
                    assertEquals(isActive, response.getIsActive());
                });

        verify(customersServiceClient, times(1))
                .patchPet(isActive, petId);
    }

    @Test
    void whenPatchPet_withNonExistentPet_thenReturnBadRequest() {
        String petId = "non-existent-pet";
        String isActive = "false";

        when(customersServiceClient.patchPet(isActive, petId))
                .thenReturn(Mono.empty());

        client.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/gateway/pets/{petId}/active")
                        .queryParam("isActive", isActive)
                        .build(petId))
                .exchange()
                .expectStatus().isBadRequest();

        verify(customersServiceClient, times(1))
                .patchPet(isActive, petId);
    }

    @Test
    void whenUpdatePet_withNonExistentPet_thenReturnNotFound() {
        String petId = "non-existent-pet";

        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("Updated Pet");
        petRequest.setBirthDate(birthDate);
        petRequest.setPetTypeId("5");
        petRequest.setWeight("12.0");
        petRequest.setIsActive("true");

        when(customersServiceClient.updatePet(any(Mono.class), eq(petId)))
                .thenReturn(Mono.empty());

        client.put()
                .uri("/api/gateway/pets/{petId}", petId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(petRequest))
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1))
                .updatePet(any(Mono.class), eq(petId));
    }

    @Test
    void whenUpdatePetForOwner_withNonExistentPet_thenReturnNotFound() {
        String ownerId = "owner-123";
        String petId = "non-existent-pet";

        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("Updated Pet");
        petRequest.setBirthDate(birthDate);
        petRequest.setPetTypeId("5");
        petRequest.setWeight("12.0");
        petRequest.setIsActive("true");

        when(customersServiceClient.updatePet(any(Mono.class), eq(petId)))
                .thenReturn(Mono.empty());

        client.put()
                .uri("/api/gateway/pets/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(petRequest))
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1))
                .updatePet(any(Mono.class), eq(petId));
    }

}