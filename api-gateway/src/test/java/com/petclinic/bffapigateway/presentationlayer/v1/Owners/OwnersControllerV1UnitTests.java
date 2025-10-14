package com.petclinic.bffapigateway.presentationlayer.v1.Owners;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v1.OwnerControllerV1;
import com.petclinic.bffapigateway.presentationlayer.v1.PetControllerV1;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@WebFluxTest(
        controllers = {OwnerControllerV1.class, PetControllerV1.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtTokenFilter.class, RoleFilter.class, IsUserFilter.class}
        )
)
@AutoConfigureWebTestClient
public class OwnersControllerV1UnitTests {

    @Autowired
    private WebTestClient client;

    @MockBean
    private CustomersServiceClient customersServiceClient;

    String ownerId = "ownerId-123";
    @Test
    void whenGetAllOwners_thenReturnOwners() {
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-90");
        owner.setFirstName("John");
        owner.setLastName("Johnny");

        when(customersServiceClient.getAllOwners()).thenReturn(Flux.just(owner));

        client.get()
                .uri("/api/gateway/owners")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OwnerResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals("ownerId-90", list.get(0).getOwnerId());
                });
    }

    @Test
    void whenGetAllOwnersByPagination_thenReturnOwners() {
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-09");
        owner.setFirstName("Test");
        owner.setLastName("Test");
        owner.setAddress("Test");
        owner.setCity("Test");
        owner.setProvince("Test");
        owner.setTelephone("Test");

        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(1);

        when(customersServiceClient.getOwnersByPagination(page, size, null, null, null, null, null))
                .thenReturn(Flux.just(owner));

        client.get()
                .uri("/api/gateway/owners/owners-pagination?page=0&size=1")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(OwnerResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals("ownerId-09", list.get(0).getOwnerId());
                });
    }

    @Test
    void whenGetAllOwnersByPagination_withEmptyPageAndSize_thenReturnEmptyList() {
        when(customersServiceClient.getOwnersByPagination(null, null, null, null, null, null, null))
                .thenReturn(Flux.empty());

        client.get()
                .uri("/api/gateway/owners/owners-pagination")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(OwnerResponseDTO.class)
                .value(list -> assertEquals(0, list.size()));
    }

    @Test
    void whenGetTotalNumberOfOwners_thenReturnCount() {
        long expectedCount = 0L;
        when(customersServiceClient.getTotalNumberOfOwners()).thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/owners/owners-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(body -> assertEquals(expectedCount, body));
    }

    @Test
    void whenGetTotalNumberOfOwners_WithFilters_thenReturnCount() {
        long expectedCount = 0L;
        when(customersServiceClient.getTotalNumberOfOwnersWithFilters(null, null, null, null, null))
                .thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/owners/owners-filtered-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(body -> assertEquals(expectedCount, body));
    }

    @Test
    void whenGetOwnerByOwnerId_thenReturnOwner() {
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-123");
        owner.setFirstName("John");
        owner.setLastName("Johnny");
        owner.setAddress("111 John St");
        owner.setCity("Johnston");
        owner.setProvince("Quebec");
        owner.setTelephone("51451545144");

        when(customersServiceClient.getOwner("ownerId-123"))
                .thenReturn(Mono.just(owner));

        client.get()
                .uri("/api/gateway/owners/{ownerId}", owner.getOwnerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .value(ownerResponseDTO -> {
                    assertNotNull(ownerResponseDTO);
                    assertEquals(ownerResponseDTO.getOwnerId(), owner.getOwnerId());
                });
    }

    @Test
    void whenUpdateOwner_thenReturnUpdatedOwner() {
        String ownerId = "f470653d-05c5-4c45-b7a0-7d70f003d2ac";
        OwnerRequestDTO updatedOwnerData = new OwnerRequestDTO();
        updatedOwnerData.setOwnerId(ownerId);
        updatedOwnerData.setFirstName("UpdatedFirstName");
        updatedOwnerData.setLastName("UpdatedLastName");

        OwnerResponseDTO updatedOwner = new OwnerResponseDTO();
        updatedOwner.setOwnerId(ownerId);
        updatedOwner.setFirstName(updatedOwnerData.getFirstName());
        updatedOwner.setLastName(updatedOwnerData.getLastName());

        when(customersServiceClient.updateOwner(eq(ownerId), any(Mono.class)))
                .thenReturn(Mono.just(updatedOwner));

        client.put()
                .uri("/api/gateway/owners/" + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updatedOwnerData))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .isEqualTo(updatedOwner);

        Mockito.verify(customersServiceClient, times(1))
                .updateOwner(eq(ownerId), any(Mono.class));
    }

    @Test
    void whenDeletePet_thenReturnNoContent() {
        String petId = "petId-456";

        when(customersServiceClient.deletePet(ownerId, petId)).thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .exchange()
                .expectStatus().isNoContent();

        verify(customersServiceClient, times(1)).deletePet(ownerId, petId);
    }

    @Test
    void whenDeleteOwner_thenReturnNoContent() {

        when(customersServiceClient.deleteOwner(ownerId)).thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/owners/{ownerId}", ownerId)
                .exchange()
                .expectStatus().isNoContent();

        verify(customersServiceClient, times(1)).deleteOwner(ownerId);
    }

    @Test
    void whenCreatePetForOwner_thenReturnCreatedPet() {
        PetRequestDTO mockPetRequest = new PetRequestDTO();
        mockPetRequest.setName("Rex");
        mockPetRequest.setOwnerId(ownerId);

        PetResponseDTO mockPetResponse = new PetResponseDTO();
        mockPetResponse.setPetId("new-pet-id");
        mockPetResponse.setName("Rex");

        when(customersServiceClient.createPetForOwner(eq(ownerId), any(PetRequestDTO.class)))
                .thenReturn(Mono.just(mockPetResponse));

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(mockPetRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PetResponseDTO.class)
                .value(body -> assertEquals("Rex", body.getName()));

        verify(customersServiceClient, times(1)).createPetForOwner(eq(ownerId), any(PetRequestDTO.class));
    }

    @Test
    void whenGetPet_thenReturnPet() {

        String petId = "petId-456";

        PetResponseDTO mockPetResponse = new PetResponseDTO();
        mockPetResponse.setPetId(petId);
        mockPetResponse.setName("Whiskers");

        when(customersServiceClient.getPet(ownerId, petId))
                .thenReturn(Mono.just(mockPetResponse));

        client.get()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetResponseDTO.class)
                .value(body -> assertEquals("Whiskers", body.getName()));

        verify(customersServiceClient, times(1)).getPet(ownerId, petId);
    }

    @Test
    void whenGetPetsByOwnerId_thenReturnListOfPets() {

        PetResponseDTO pet1 = new PetResponseDTO();
        pet1.setName("Rocky");
        PetResponseDTO pet2 = new PetResponseDTO();
        pet2.setName("Bella");

        when(customersServiceClient.getPetsByOwnerId(ownerId)).thenReturn(Flux.just(pet1, pet2));

        client.get()
                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(PetResponseDTO.class)
                .hasSize(2)
                .value(list -> assertEquals("Rocky", list.get(0).getName()));

        verify(customersServiceClient, times(1)).getPetsByOwnerId(ownerId);
    }

    @Test
    void whenGetOwnerPhoto_thenReturnPhoto() {
        byte[] mockPhotoBytes = "mockPhotoData".getBytes();

        when(customersServiceClient.getOwnerPhoto(ownerId))
                .thenReturn(Mono.just(mockPhotoBytes));

        client.get()
                .uri("/api/gateway/owners/{ownerId}/photos", ownerId)
                .accept(MediaType.IMAGE_PNG)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_PNG)
                .expectBody(byte[].class)
                .value(bytes -> assertArrayEquals(mockPhotoBytes, bytes));

        verify(customersServiceClient, times(1)).getOwnerPhoto(ownerId);
    }

    @Test
    void whenGetOwnerPhotoNotFound_thenReturn404() {
        when(customersServiceClient.getOwnerPhoto(ownerId))
                .thenReturn(Mono.empty());

        client.get()
                .uri("/api/gateway/owners/{ownerId}/photos", ownerId)
                .accept(MediaType.IMAGE_PNG)
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1)).getOwnerPhoto(ownerId);
    }

}
