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
        String petId = "petId-123";

        PetResponseDTO expectedResponse = new PetResponseDTO();
        expectedResponse.setPetId(petId);
        expectedResponse.setName("Fluffy");
        expectedResponse.setBirthDate(birthDate);
        expectedResponse.setPetTypeId("5");
        expectedResponse.setWeight("10.5");
        expectedResponse.setIsActive("true");

        when(customersServiceClient.getPetByPetId(petId))
                .thenReturn(Mono.just(expectedResponse));

        client.get()
                .uri("/api/gateway/pets/{petId}", petId)
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
                .getPetByPetId(petId);
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

        when(customersServiceClient.getPetByPetId(petId))
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
                .getPetByPetId(petId);
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
}