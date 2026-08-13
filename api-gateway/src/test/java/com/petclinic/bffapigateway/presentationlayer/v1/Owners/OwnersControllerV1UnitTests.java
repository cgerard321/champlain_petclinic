package com.petclinic.bffapigateway.presentationlayer.v1.Owners;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Files.FileDetails;
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
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
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

        when(customersServiceClient.getOwner("ownerId-123", false))
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
        updatedOwnerData.setFirstName("UpdatedFirstName");
        updatedOwnerData.setLastName("UpdatedLastName");

        OwnerResponseDTO updatedOwner = new OwnerResponseDTO();
        updatedOwner.setOwnerId(ownerId);
        updatedOwner.setFirstName(updatedOwnerData.getFirstName());
        updatedOwner.setLastName(updatedOwnerData.getLastName());

        when(customersServiceClient.updateOwner(eq(ownerId), any()))
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
                .updateOwner(eq(ownerId), any());
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
    void whenGetOwnerWithPhoto_thenReturnOwnerWithPhotoData() {
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId(ownerId);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        FileDetails photo = new FileDetails();
        photo.setFileData("mockPhotoData".getBytes());
        photo.setFileType("image/png");
        owner.setPhoto(photo);

        when(customersServiceClient.getOwner(ownerId, true))
                .thenReturn(Mono.just(owner));

        client.get()
                .uri("/api/gateway/owners/{ownerId}?includePhoto=true", ownerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OwnerResponseDTO.class)
                .value(ownerResponse -> {
                    assertEquals(ownerId, ownerResponse.getOwnerId());
                    assertEquals("John", ownerResponse.getFirstName());
                    assertEquals("Doe", ownerResponse.getLastName());
                    assertNotNull(ownerResponse.getPhoto());
                    assertArrayEquals("mockPhotoData".getBytes(), ownerResponse.getPhoto().getFileData());
                    assertEquals("image/png", ownerResponse.getPhoto().getFileType());
                });

        verify(customersServiceClient, times(1)).getOwner(ownerId, true);
    }

    @Test
    void whenGetOwnerWithoutPhoto_thenReturnOwnerWithoutPhotoData() {
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId(ownerId);
        owner.setFirstName("John");
        owner.setLastName("Doe");

        when(customersServiceClient.getOwner(ownerId, false))
                .thenReturn(Mono.just(owner));

        client.get()
                .uri("/api/gateway/owners/{ownerId}?includePhoto=false", ownerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OwnerResponseDTO.class)
                .value(ownerResponse -> {
                    assertEquals(ownerId, ownerResponse.getOwnerId());
                    assertEquals("John", ownerResponse.getFirstName());
                    assertEquals("Doe", ownerResponse.getLastName());
                    assertNull(ownerResponse.getPhoto());
                });

        verify(customersServiceClient, times(1)).getOwner(ownerId, false);
    }

    @Test
    void whenGetOwnerWithDefaultPhoto_thenReturnOwnerWithDefaultPhoto() {
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId(ownerId);
        owner.setFirstName("John");
        owner.setLastName("Doe");

        when(customersServiceClient.getOwner(ownerId, false))
                .thenReturn(Mono.just(owner));

        client.get()
                .uri("/api/gateway/owners/{ownerId}", ownerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OwnerResponseDTO.class)
                .value(ownerResponse -> {
                    assertEquals(ownerId, ownerResponse.getOwnerId());
                    assertEquals("John", ownerResponse.getFirstName());
                    assertEquals("Doe", ownerResponse.getLastName());
                });

        verify(customersServiceClient, times(1)).getOwner(ownerId, false);
    }

    @Test
    void whenGetAllOwnersByPagination_withFilters_thenReturnFilteredOwners() {
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("owner1");
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setCity("Montreal");
        owner.setTelephone("5551234567");

        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(5);

        when(customersServiceClient.getOwnersByPagination(page, size, "owner1", "John", "Doe", "5551234567", "Montreal"))
                .thenReturn(Flux.just(owner));

        client.get()
                .uri("/api/gateway/owners/owners-pagination?page=0&size=5&ownerId=owner1&firstName=John&lastName=Doe&phoneNumber=5551234567&city=Montreal")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(OwnerResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals("owner1", list.get(0).getOwnerId());
                });
    }

    @Test
    void whenGetTotalNumberOfOwnersWithFilters_withFilters_thenReturnFilteredCount() {
        long expectedCount = 5L;

        when(customersServiceClient.getTotalNumberOfOwnersWithFilters("owner1", "John", "Doe", "5551234567", "Montreal"))
                .thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/owners/owners-filtered-count?ownerId=owner1&firstName=John&lastName=Doe&phoneNumber=5551234567&city=Montreal")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(body -> assertEquals(expectedCount, body));
    }

    @Test
    void whenGetOwnerByOwnerId_withNonExistentOwner_thenReturnNotFound() {
        when(customersServiceClient.getOwner("nonexistent", false))
                .thenReturn(Mono.empty());

        client.get()
                .uri("/api/gateway/owners/{ownerId}", "nonexistent")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenUpdateOwner_withNonExistentOwner_thenReturnNotFound() {
        OwnerRequestDTO requestDTO = new OwnerRequestDTO();
        requestDTO.setFirstName("John");
        requestDTO.setLastName("Doe");

        when(customersServiceClient.updateOwner(eq("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a"), any()))
                .thenReturn(Mono.empty());

        client.put()
                .uri("/api/gateway/owners/{ownerId}", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetPetsByOwnerId_withNoPets_thenReturnEmptyList() {
        when(customersServiceClient.getPetsByOwnerId(ownerId))
                .thenReturn(Flux.empty());

        client.get()
                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(PetResponseDTO.class)
                .hasSize(0);
    }

    @Test
    void whenUpdateOwnerPhoto_withValidPhoto_thenReturnUpdatedOwner() {
        FileDetails photoRequest = FileDetails.builder()
                .fileName("profile.jpeg")
                .fileType("image/jpeg")
                .fileData("mockPhotoData".getBytes())
                .build();

        OwnerResponseDTO updatedOwner = new OwnerResponseDTO();
        updatedOwner.setOwnerId(ownerId);
        updatedOwner.setFirstName("John");
        updatedOwner.setLastName("Doe");
        FileDetails photo = new FileDetails();
        photo.setFileData("mockPhotoData".getBytes());
        photo.setFileType("image/jpeg");
        updatedOwner.setPhoto(photo);

        when(customersServiceClient.updateOwnerPhoto(eq(ownerId), any()))
                .thenReturn(Mono.just(updatedOwner));

        client.patch()
                .uri("/api/gateway/owners/{ownerId}/photo", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(photoRequest))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .value(body -> {
                    assertNotNull(body);
                    assertEquals(ownerId, body.getOwnerId());
                    assertNotNull(body.getPhoto());
                    assertEquals("image/jpeg", body.getPhoto().getFileType());
                    assertArrayEquals("mockPhotoData".getBytes(), body.getPhoto().getFileData());
                });

        verify(customersServiceClient, times(1)).updateOwnerPhoto(eq(ownerId), any());
    }

    @Test
    void whenUpdateOwnerPhoto_withNonExistentOwner_thenReturnNotFound() {
        FileDetails photoRequest = FileDetails.builder()
                .fileName("profile.jpeg")
                .fileType("image/jpeg")
                .fileData("mockPhotoData".getBytes())
                .build();

        when(customersServiceClient.updateOwnerPhoto(eq("nonexistent"), any()))
                .thenReturn(Mono.empty());

        client.patch()
                .uri("/api/gateway/owners/{ownerId}/photo", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(photoRequest))
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1)).updateOwnerPhoto(eq("nonexistent"), any());
    }

    @Test
    void whenUpdateOwnerPhoto_withPngPhoto_thenReturnUpdatedOwner() {
        FileDetails photoRequest = FileDetails.builder()
                .fileName("avatar.png")
                .fileType("image/png")
                .fileData("pngPhotoData".getBytes())
                .build();

        OwnerResponseDTO updatedOwner = new OwnerResponseDTO();
        updatedOwner.setOwnerId(ownerId);
        updatedOwner.setFirstName("Jane");
        updatedOwner.setLastName("Smith");
        FileDetails photo = new FileDetails();
        photo.setFileData("pngPhotoData".getBytes());
        photo.setFileType("image/png");
        updatedOwner.setPhoto(photo);

        when(customersServiceClient.updateOwnerPhoto(eq(ownerId), any()))
                .thenReturn(Mono.just(updatedOwner));

        client.patch()
                .uri("/api/gateway/owners/{ownerId}/photo", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(photoRequest))
                .exchange()
                .expectStatus().isOk()
                .expectBody(OwnerResponseDTO.class)
                .value(body -> {
                    assertNotNull(body);
                    assertEquals(ownerId, body.getOwnerId());
                    assertNotNull(body.getPhoto());
                    assertEquals("image/png", body.getPhoto().getFileType());
                });

        verify(customersServiceClient, times(1)).updateOwnerPhoto(eq(ownerId), any());
    }

    @Test
    void whenDeleteOwnerPhoto_thenReturnOk() {
        OwnerResponseDTO responseWithoutPhoto = new OwnerResponseDTO();
        responseWithoutPhoto.setOwnerId(ownerId);
        responseWithoutPhoto.setPhoto(null);

        when(customersServiceClient.deleteOwnerPhoto(ownerId)).thenReturn(Mono.just(responseWithoutPhoto));

        client.delete()
                .uri("/api/gateway/owners/{ownerId}/photo", ownerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OwnerResponseDTO.class)
                .value(body -> {
                    assertEquals(ownerId, body.getOwnerId());
                    assertNull(body.getPhoto());
                });

        verify(customersServiceClient, times(1)).deleteOwnerPhoto(ownerId);
    }

    @Test
    void whenDeleteOwnerPhoto_withNonExistentOwner_thenReturnNotFound() {
        when(customersServiceClient.deleteOwnerPhoto(ownerId)).thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/owners/{ownerId}/photo", ownerId)
                .exchange()
                .expectStatus().isNotFound();
        verify(customersServiceClient, times(1)).deleteOwnerPhoto(ownerId);
    }

    @Test
    void whenDeletePetPhotoForOwner_withValidPet_thenReturnOk() {
        String petId = "pet-id-123";
        PetResponseDTO petResponseDTO = new PetResponseDTO();
        petResponseDTO.setPetId(petId);
        petResponseDTO.setName("Test Pet");
        petResponseDTO.setPhoto(null);

        when(customersServiceClient.deletePetPhoto(petId)).thenReturn(Mono.just(petResponseDTO));

        client.patch()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}/photo", ownerId, petId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetResponseDTO.class)
                .value(body -> {
                    assertEquals(petId, body.getPetId());
                    assertEquals("Test Pet", body.getName());
                    assertNull(body.getPhoto());
                });

        verify(customersServiceClient, times(1)).deletePetPhoto(petId);
    }

    @Test
    void whenDeletePetPhotoForOwner_withNonExistentPet_thenReturnNotFound() {
        String petId = "non-existent-pet-id";
        
        when(customersServiceClient.deletePetPhoto(petId)).thenReturn(Mono.empty());

        client.patch()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}/photo", ownerId, petId)
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1)).deletePetPhoto(petId);
    }

    @Test
    void whenCreatePetForOwner_withValidRequest_thenReturnCreated() {
        String petId = "pet-id-123";
        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("New Pet");
        petRequest.setPetTypeId("pt-1");
        petRequest.setWeight("5.0");
        petRequest.setIsActive("true");

        PetResponseDTO createdPet = new PetResponseDTO();
        createdPet.setPetId(petId);
        createdPet.setName("New Pet");
        createdPet.setPetTypeId("pt-1");
        createdPet.setWeight("5.0");
        createdPet.setIsActive("true");

        when(customersServiceClient.createPetForOwner(ownerId, petRequest))
                .thenReturn(Mono.just(createdPet));

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(petRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PetResponseDTO.class)
                .value(body -> {
                    assertEquals(petId, body.getPetId());
                    assertEquals("New Pet", body.getName());
                });

        verify(customersServiceClient, times(1)).createPetForOwner(ownerId, petRequest);
    }

    @Test
    void whenCreatePetForOwner_withInvalidRequest_thenReturnBadRequest() {
        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("New Pet");

        when(customersServiceClient.createPetForOwner(ownerId, petRequest))
                .thenReturn(Mono.empty());

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(petRequest))
                .exchange()
                .expectStatus().isBadRequest();

        verify(customersServiceClient, times(1)).createPetForOwner(ownerId, petRequest);
    }

    @Test
    void whenGetPet_withValidIds_thenReturnPet() {
        String petId = "pet-id-123";
        PetResponseDTO pet = new PetResponseDTO();
        pet.setPetId(petId);
        pet.setName("Test Pet");

        when(customersServiceClient.getPet(ownerId, petId))
                .thenReturn(Mono.just(pet));

        client.get()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetResponseDTO.class)
                .value(body -> {
                    assertEquals(petId, body.getPetId());
                    assertEquals("Test Pet", body.getName());
                });

        verify(customersServiceClient, times(1)).getPet(ownerId, petId);
    }

    @Test
    void whenGetPet_withNonExistentPet_thenReturnNotFound() {
        String petId = "non-existent-pet-id";

        when(customersServiceClient.getPet(ownerId, petId))
                .thenReturn(Mono.empty());

        client.get()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1)).getPet(ownerId, petId);
    }

    @Test
    void whenDeletePet_withValidId_thenReturnNoContent() {
        String petId = "pet-id-123";

        PetResponseDTO deletedPet = new PetResponseDTO();
        deletedPet.setPetId(petId);
        deletedPet.setName("Deleted Pet");

        when(customersServiceClient.deletePet(ownerId, petId))
                .thenReturn(Mono.just(deletedPet));

        client.delete()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .exchange()
                .expectStatus().isNoContent();

        verify(customersServiceClient, times(1)).deletePet(ownerId, petId);
    }

    @Test
    void whenDeletePet_withNonExistentPet_thenReturnNotFound() {
        String petId = "non-existent-pet-id";

        when(customersServiceClient.deletePet(ownerId, petId))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}", ownerId, petId)
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1)).deletePet(ownerId, petId);
    }

    @Test
    void whenGetAllPets_thenReturnListOfPets() {
        PetResponseDTO pet1 = new PetResponseDTO();
        pet1.setPetId("pet-1");
        pet1.setName("Fluffy");
        
        PetResponseDTO pet2 = new PetResponseDTO();
        pet2.setPetId("pet-2");
        pet2.setName("Buddy");

        when(customersServiceClient.getAllPets()).thenReturn(Flux.just(pet1, pet2));

        client.get()
                .uri("/api/gateway/pets")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(PetResponseDTO.class)
                .hasSize(2)
                .value(list -> {
                    assertEquals("pet-1", list.get(0).getPetId());
                    assertEquals("Fluffy", list.get(0).getName());
                    assertEquals("pet-2", list.get(1).getPetId());
                    assertEquals("Buddy", list.get(1).getName());
                });

        verify(customersServiceClient, times(1)).getAllPets();
    }

    @Test
    void whenPatchPet_withValidRequest_thenReturnOk() {
        String petId = "pet-id-123";
        String isActive = "false";
        
        PetResponseDTO updatedPet = new PetResponseDTO();
        updatedPet.setPetId(petId);
        updatedPet.setName("Updated Pet");
        updatedPet.setIsActive(isActive);

        when(customersServiceClient.patchPet(isActive, petId))
                .thenReturn(Mono.just(updatedPet));

        client.patch()
                .uri("/api/gateway/pets/{petId}/active?isActive={isActive}", petId, isActive)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetResponseDTO.class)
                .value(body -> {
                    assertEquals(petId, body.getPetId());
                    assertEquals("Updated Pet", body.getName());
                    assertEquals(isActive, body.getIsActive());
                });

        verify(customersServiceClient, times(1)).patchPet(isActive, petId);
    }

    @Test
    void whenPatchPet_withInvalidRequest_thenReturnBadRequest() {
        String petId = "pet-id-123";
        String isActive = "invalid";

        when(customersServiceClient.patchPet(isActive, petId))
                .thenReturn(Mono.empty());

        client.patch()
                .uri("/api/gateway/pets/{petId}/active?isActive={isActive}", petId, isActive)
                .exchange()
                .expectStatus().isBadRequest();

        verify(customersServiceClient, times(1)).patchPet(isActive, petId);
    }
}
