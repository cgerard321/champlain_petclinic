package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
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

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static com.mongodb.assertions.Assertions.fail;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
class PetTypeControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PetTypeRepo petTypeRepo;

    @MockBean
    private FilesServiceClient filesServiceClient;

    PetType petTypeEntity2 = buildPetType2();
    String PUBLIC_PETTYPE_ID = petTypeEntity2.getPetTypeId();

    @Test
    void getAllPetTypes_shouldSucceed() {
        webTestClient.get()
                .uri("/owners/petTypes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(PetTypeResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertTrue(list.size() >= 0, "List should not be null and can have zero or more elements");
                });
    }

    @Test
    void whenCreatePetType_ShouldReturnCreated() {
        PetTypeRequestDTO request = new PetTypeRequestDTO("Bird", "Flies");

        webTestClient.post()
                .uri("/owners/petTypes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), PetTypeRequestDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PetTypeResponseDTO.class)
                .value(created -> {
                    assertNotNull(created);
                    assertNotNull(created.getPetTypeId());
                    assertEquals("Bird", created.getName());
                    assertEquals("Flies", created.getPetTypeDescription());
                });
    }

    @Test
    void updatePetType() {
        PetType testPetType = PetType.builder()
                .id("test-id-123")
                .petTypeId("test-petTypeId-123")
                .name("Original Dog")
                .petTypeDescription("Original Mammal")
                .build();

        Publisher<PetType> setup = petTypeRepo.deleteAll().thenMany(petTypeRepo.save(testPetType));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();

        PetTypeRequestDTO updateRequest = PetTypeRequestDTO.builder()
                .name("Updated Dog")
                .petTypeDescription("Updated Mammal")
                .build();

        webTestClient.put().uri("/owners/petTypes/" + testPetType.getPetTypeId())
                .body(Mono.just(updateRequest), PetTypeRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petTypeId").isEqualTo(testPetType.getPetTypeId())
                .jsonPath("$.name").isEqualTo("Updated Dog")
                .jsonPath("$.petTypeDescription").isEqualTo("Updated Mammal");
    }

    @Test
    void getOwnerByOwnerId() {
        Publisher<PetType> setup = petTypeRepo.deleteAll().thenMany(petTypeRepo.save(petTypeEntity2));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        webTestClient.get().uri("/owners/petTypes/" + PUBLIC_PETTYPE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PetTypeResponseDTO.class)
                .value(petTypeResponseDTO -> {
                    assertNotNull(petTypeResponseDTO);
                    assertEquals(petTypeResponseDTO.getPetTypeId(), petTypeEntity2.getPetTypeId());
                    assertEquals(petTypeResponseDTO.getName(), petTypeEntity2.getName());
                    assertEquals(petTypeResponseDTO.getPetTypeDescription(), petTypeEntity2.getPetTypeDescription());
                });
    }

    @Test
    void deletePetType_ShouldReturnNoContent() {
        PetType petType = PetType.builder()
                .id("4283c9b8-4ffd-4866-a5ed-287117c60a40")
                .petTypeId("4283c9b8-4ffd-4866-a5ed-287117c60a40")
                .name("Cat")
                .petTypeDescription("Mammal")
                .build();

        try {
            PetType savedPetType = petTypeRepo.save(petType).block();
            assertNotNull(savedPetType);

            webTestClient.delete()
                    .uri("/owners/petTypes/" + savedPetType.getPetTypeId())
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();

            StepVerifier.create(
                            petTypeRepo.findByPetTypeId(savedPetType.getPetTypeId())
                                    .timeout(Duration.ofSeconds(5))
                                    .onErrorMap(TimeoutException.class,
                                            e -> new RuntimeException("Database query timed out", e)))
                    .expectComplete()
                    .verify();

        } catch (NotFoundException | InvalidInputException e) {
            fail("Unexpected exception: " + e.getMessage());
        } finally {
            try {
                petTypeRepo.deleteByPetTypeId(petType.getPetTypeId()).block();
            } catch (Exception cleanup) {
                System.err.println("Cleanup failed: " + cleanup.getMessage());
            }
        }
    }

    @Test
    void getPetTypesPagination_WithValidParameters_ShouldReturnPaginatedResults() {
        webTestClient.get()
                .uri("/owners/petTypes/pet-types-pagination?page=0&size=2")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(PetTypeResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertTrue(list.size() <= 2);
                });
    }

    @Test
    void getPetTypesPagination_WithNameFilter_ShouldReturnFilteredResults() {
        webTestClient.get()
                .uri("/owners/petTypes/pet-types-pagination?page=0&size=10&name=Dog")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(PetTypeResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    list.forEach(petType ->
                            assertTrue(petType.getName().toLowerCase().contains("dog")));
                });
    }

    @Test
    void getPetTypesPagination_WithDescriptionFilter_ShouldReturnFilteredResults() {
        webTestClient.get()
                .uri("/owners/petTypes/pet-types-pagination?page=0&size=10&description=Mammal")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(PetTypeResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    list.forEach(petType ->
                            assertTrue(petType.getPetTypeDescription().toLowerCase().contains("mammal")));
                });
    }

    @Test
    void getPetTypesPagination_WithPetTypeIdFilter_ShouldReturnExactMatch() {
        webTestClient.get()
                .uri("/owners/petTypes/pet-types-pagination?page=0&size=10&petTypeId=1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(PetTypeResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertTrue(list.size() <= 1);
                    if (!list.isEmpty()) {
                        assertEquals("1", list.get(0).getPetTypeId());
                    }
                });
    }

    @Test
    void getPetTypesCount_ShouldReturnTotalCount() {
        webTestClient.get()
                .uri("/owners/petTypes/pet-types-count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Long.class)
                .value(count -> {
                    assertNotNull(count);
                    assertTrue(count >= 0);
                });
    }

    @Test
    void getPetTypesFilteredCount_WithNameFilter_ShouldReturnFilteredCount() {
        webTestClient.get()
                .uri("/owners/petTypes/pet-types-filtered-count?name=Dog")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Long.class)
                .value(count -> {
                    assertNotNull(count);
                    assertTrue(count >= 0);
                });
    }

    @Test
    void getPetTypesPagination_WithEmptyFilters_ShouldReturnAllResults() {
        webTestClient.get()
                .uri("/owners/petTypes/pet-types-pagination?page=0&size=100")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(PetTypeResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertTrue(list.size() >= 0);
                });
    }

    private PetType buildPetType() {
        return PetType.builder().id("10").name("TestType").build();
    }

    private PetType buildPetType2() {
        return PetType.builder()
                .id("10")
                .petTypeId("petTypeId-1234")
                .name("Dog")
                .petTypeDescription("Mammal")
                .build();
    }
}
