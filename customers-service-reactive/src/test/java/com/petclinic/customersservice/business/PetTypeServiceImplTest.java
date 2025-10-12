package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import com.petclinic.customersservice.presentationlayer.PetTypeRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port=0"})
@AutoConfigureWebTestClient
class PetTypeServiceImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PetTypeService petTypeService;

    @MockBean
    private PetTypeRepo petTypeRepo;

    @MockBean
    private FilesServiceClient filesServiceClient;

    @Test
    void deletePetTypeByPetTypeId_ShouldDeleteSuccessfully() {
        String petTypeId = "4283c9b8-4ffd-4866-a5ed-287117c60a40";
        when(petTypeRepo.deleteByPetTypeId(petTypeId)).thenReturn(Mono.empty());

        Mono<Void> result = petTypeService.deletePetTypeByPetTypeId(petTypeId);

        StepVerifier.create(result).verifyComplete();
        verify(petTypeRepo).deleteByPetTypeId(petTypeId);
    }

    @Test
    void deletePetTypeByPetTypeId_WhenPetTypeNotFound_ShouldComplete() {
        String petTypeId = "non-existent-id";
        when(petTypeRepo.deleteByPetTypeId(petTypeId)).thenReturn(Mono.empty());

        StepVerifier.create(petTypeService.deletePetTypeByPetTypeId(petTypeId))
                .verifyComplete();
    }

    @Test
    void whenAddPetType_missingName() {
        PetTypeRequestDTO dto = new PetTypeRequestDTO(null, "Desc");
        StepVerifier.create(petTypeService.addPetType(Mono.just(dto)))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void whenAddPetType_missingDescription() {
        PetTypeRequestDTO dto = new PetTypeRequestDTO("Name", null);
        StepVerifier.create(petTypeService.addPetType(Mono.just(dto)))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void whenAddPetType_ShouldInsertAndReturnPetType() {
        PetType petTypeToAdd = buildPetType();
        when(petTypeRepo.insert(any(PetType.class)))
                .thenReturn(Mono.just(petTypeToAdd));

        Mono<PetType> result = petTypeService.insertPetType(Mono.just(petTypeToAdd));

        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertNotNull(saved);
                    assertEquals(petTypeToAdd.getPetTypeId(), saved.getPetTypeId());
                    assertEquals(petTypeToAdd.getName(), saved.getName());
                    assertEquals(petTypeToAdd.getPetTypeDescription(), saved.getPetTypeDescription());
                })
                .verifyComplete();

        verify(petTypeRepo).insert(any(PetType.class));
    }

    @Test
    void getAllPetTypesPagination_WithNoFilters_ShouldReturnPaginatedResults() {
        PetType petType1 = buildPetType("1", "Dog", "Mammal");
        PetType petType2 = buildPetType("2", "Cat", "Mammal");
        PetType petType3 = buildPetType("3", "Bird", "Bird");
        when(petTypeRepo.findAll()).thenReturn(Flux.just(petType1, petType2, petType3));

        Pageable pageable = PageRequest.of(0, 2);

        StepVerifier.create(petTypeService.getAllPetTypesPagination(pageable, null, null, null))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getAllPetTypesPagination_WithNameFilter_ShouldReturnFilteredResults() {
        PetType petType1 = buildPetType("1", "Dog", "Mammal");
        PetType petType2 = buildPetType("2", "Cat", "Mammal");
        PetType petType3 = buildPetType("3", "Dog", "Mammal");
        when(petTypeRepo.findAll()).thenReturn(Flux.just(petType1, petType2, petType3));

        Pageable pageable = PageRequest.of(0, 10);

        StepVerifier.create(petTypeService.getAllPetTypesPagination(pageable, null, "Dog", null))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getAllPetTypesPagination_WithDescriptionFilter_ShouldReturnFilteredResults() {
        PetType petType1 = buildPetType("1", "Dog", "Mammal");
        PetType petType2 = buildPetType("2", "Cat", "Mammal");
        PetType petType3 = buildPetType("3", "Bird", "Bird");
        when(petTypeRepo.findAll()).thenReturn(Flux.just(petType1, petType2, petType3));

        Pageable pageable = PageRequest.of(0, 10);

        StepVerifier.create(petTypeService.getAllPetTypesPagination(pageable, null, null, "Mammal"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getAllPetTypesPagination_WithPetTypeIdFilter_ShouldReturnExactMatch() {
        PetType petType1 = buildPetType("1", "Dog", "Mammal");
        PetType petType2 = buildPetType("2", "Cat", "Mammal");
        when(petTypeRepo.findAll()).thenReturn(Flux.just(petType1, petType2));

        Pageable pageable = PageRequest.of(0, 10);

        StepVerifier.create(petTypeService.getAllPetTypesPagination(pageable, "1", null, null))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAllPetTypesPagination_WithMultipleFilters_ShouldReturnFilteredResults() {
        PetType petType1 = buildPetType("1", "Dog", "Mammal");
        PetType petType2 = buildPetType("2", "Cat", "Mammal");
        PetType petType3 = buildPetType("3", "Dog", "Bird");
        when(petTypeRepo.findAll()).thenReturn(Flux.just(petType1, petType2, petType3));

        Pageable pageable = PageRequest.of(0, 10);

        StepVerifier.create(petTypeService.getAllPetTypesPagination(pageable, null, "Dog", "Mammal"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getTotalNumberOfPetTypesWithFilters_WithNoFilters_ShouldReturnTotalCount() {
        PetType petType1 = buildPetType("1", "Dog", "Mammal");
        PetType petType2 = buildPetType("2", "Cat", "Mammal");
        when(petTypeRepo.findAll()).thenReturn(Flux.just(petType1, petType2));

        StepVerifier.create(petTypeService.getTotalNumberOfPetTypesWithFilters(null, null, null))
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void getTotalNumberOfPetTypesWithFilters_WithNameFilter_ShouldReturnFilteredCount() {
        PetType petType1 = buildPetType("1", "Dog", "Mammal");
        PetType petType2 = buildPetType("2", "Cat", "Mammal");
        PetType petType3 = buildPetType("3", "Dog", "Bird");
        when(petTypeRepo.findAll()).thenReturn(Flux.just(petType1, petType2, petType3));

        StepVerifier.create(petTypeService.getTotalNumberOfPetTypesWithFilters(null, "Dog", null))
                .expectNext(2L)
                .verifyComplete();
    }


    private PetType buildPetType() {
        return PetType.builder()
                .id("10")
                .petTypeId("petType-Id-123")
                .name("TestType")
                .petTypeDescription("Mammal")
                .build();
    }

    private PetType buildPetType(String id, String name, String description) {
        return PetType.builder()
                .id(id)
                .petTypeId(id)
                .name(name)
                .petTypeDescription(description)
                .build();
    }
}
