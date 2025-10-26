package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.domainclientlayer.FileRequestDTO;
import com.petclinic.customersservice.domainclientlayer.FileResponseDTO;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
class PetControllerIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private PetRepo repo;

    @MockBean
    private FilesServiceClient filesServiceClient;

    private final Pet petEntity = buildPet();
    private final PetRequestDTO petRequestDTO = buildPetRequest();
    private final String PET_ID = petEntity.getPetId();
    private String validPetId;

    @BeforeEach
    void setUp() {
        repo.deleteAll().block();
        repo.save(petEntity).block();
        when(filesServiceClient.deleteFile(anyString())).thenReturn(Mono.empty());
    }

    @Test
    void deletePetByPetId() {
        Publisher<Pet> setup = repo.save(petEntity);
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();

        client.delete()
                .uri("/pets/" + PET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getAllPets() {
        Publisher<Pet> setup = repo.deleteAll().thenMany(repo.save(petEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();

        client.get()
                .uri("/pets")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(PetResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                });
    }

    @Test
    void getPetByPetId() {
        Mono<Pet> petMono = Mono.from(repo.findAll()
                .doOnNext(pet -> validPetId = pet.getPetId()));

        StepVerifier.create(petMono)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/pets/{petId}", validPetId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petId").isEqualTo(validPetId);
    }

    @Test
    void updatePetByPetId() {
        Publisher<Pet> setup = repo.deleteAll().thenMany(repo.save(petEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();

        client.put()
                .uri("/pets/" + PET_ID)
                .body(Mono.just(petRequestDTO), PetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petId").isEqualTo(petEntity.getPetId())
                .jsonPath("$.name").isEqualTo(petEntity.getName())
                .jsonPath("$.petTypeId").isEqualTo(petEntity.getPetTypeId())
                .jsonPath("$.ownerId").isEqualTo(petEntity.getOwnerId())
                .jsonPath("$.isActive").isEqualTo(petEntity.getIsActive());
    }

    @Test
    void insertPet() {
        Publisher<Void> setup = repo.deleteAll();
        StepVerifier.create(setup).expectNextCount(0).verifyComplete();

        client.post()
                .uri("/pets")
                .body(Mono.just(petRequestDTO), PetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo(petEntity.getName())
                .jsonPath("$.petTypeId").isEqualTo(petEntity.getPetTypeId())
                .jsonPath("$.ownerId").isEqualTo(petEntity.getOwnerId())
                .jsonPath("$.weight").isEqualTo(petEntity.getWeight())
                .jsonPath("$.isActive").isEqualTo(petEntity.getIsActive());
    }

    @Test
    void updatePetIsActive() {
        Publisher<Pet> setup = repo.deleteAll().thenMany(repo.save(petEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();

        client.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/pets/{id}/active")
                        .queryParam("isActive", true)
                        .build(PET_ID))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.isActive").isEqualTo(petEntity.getIsActive());
    }

    @Test
    void deletePetPhoto_WithExistingPet_ShouldReturnOk() {
        Publisher<Pet> setup = repo.deleteAll().thenMany(repo.save(petEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();

        client.patch()
                .uri("/pets/{petId}/photo", PET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petId").isEqualTo(petEntity.getPetId())
                .jsonPath("$.name").isEqualTo(petEntity.getName())
                .jsonPath("$.photo").isEmpty();
    }

    @Test
    void deletePetPhoto_WithNonExistentPet_ShouldReturnNotFound() {
        String nonExistentPetId = "00000000-0000-0000-0000-000000000000";

        client.patch()
                .uri("/pets/{petId}/photo", nonExistentPetId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePetPhoto_WithInvalidPetId_ShouldReturnUnprocessableEntity() {
        String invalidPetId = "invalid-id";

        client.patch()
                .uri("/pets/{petId}/photo", invalidPetId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422);
    }
    @Test
    void addPetPhoto_WithValidPet_ShouldReturnCreated() {
        byte[] imageData = "fake-image".getBytes(StandardCharsets.UTF_8);
        FileRequestDTO fileRequest = new FileRequestDTO("photo.png", "image/png", imageData);
        FileResponseDTO fileResponse = new FileResponseDTO("photo-xyz", "photo.png", "image/png", imageData);

        when(filesServiceClient.addFile(any(FileRequestDTO.class))).thenReturn(Mono.just(fileResponse));

        client.patch()
                .uri("/pets/{petId}/photos", PET_ID)
                .body(Mono.just(fileRequest), FileRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petId").isEqualTo(PET_ID)
                .jsonPath("$.photo.fileId").isEqualTo("photo-xyz")
                .jsonPath("$.photo.fileName").isEqualTo("photo.png");
    }
    @Test
    void addPetPhoto_WithNonExistentPet_ShouldReturnNotFound() {
        String nonExistentPetId = "99999999-9999-9999-9999-999999999999";
        byte[] imageData = "fake-image".getBytes(StandardCharsets.UTF_8);
        FileRequestDTO fileRequest = new FileRequestDTO("photo.png", "image/png", imageData);

        when(filesServiceClient.addFile(any(FileRequestDTO.class)))
                .thenReturn(Mono.just(new FileResponseDTO("photo-xyz", "photo.png", "image/png", imageData)));

        client.patch()
                .uri("/pets/{petId}/photos", nonExistentPetId)
                .body(Mono.just(fileRequest), FileRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
    private Pet buildPet() {
        return Pet.builder()
                .id("123")
                .petId("de92af81-0135-4cd8-8cda-343f681728a3")
                .name("leonardo")
                .birthDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .ownerId("54c76b87-e598-4f26-ac63-dcb8a9571b08")
                .petTypeId("f24969bc-0009-4f02-99c9-9db426d872f3")
                .photoId(null)
                .weight("5.0")
                .isActive("true")
                .build();
    }

    private PetRequestDTO buildPetRequest() {
        return PetRequestDTO.builder()
                .name("leonardo")
                .birthDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .ownerId("54c76b87-e598-4f26-ac63-dcb8a9571b08")
                .petTypeId("f24969bc-0009-4f02-99c9-9db426d872f3")
                .weight("5.0")
                .isActive("true")
                .build();
    }
}
