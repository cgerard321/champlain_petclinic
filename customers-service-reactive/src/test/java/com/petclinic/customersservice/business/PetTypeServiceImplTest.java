package com.petclinic.customersservice.business;

import com.petclinic.customersservice.business.PetTypeService;
import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port= 0"})
@AutoConfigureWebTestClient
class PetTypeServiceImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PetTypeService petTypeService;

    @MockBean
    private PetTypeRepo petTypeRepo;




    @Test
    void deletePetTypeByPetTypeId_ShouldDeleteSuccessfully() {
        try {
            String petTypeId = "4283c9b8-4ffd-4866-a5ed-287117c60a40";
            when(petTypeRepo.deleteByPetTypeId(petTypeId)).thenReturn(Mono.empty());

            Mono<Void> result = petTypeService.deletePetTypeByPetTypeId(petTypeId);

            StepVerifier.create(result)
                    .verifyComplete();

            verify(petTypeRepo).deleteByPetTypeId(petTypeId);

        } catch (NotFoundException e) {
            fail("Unexpected NotFoundException: " + e.getMessage());
        } catch (InvalidInputException e) {
            fail("Unexpected InvalidInputException: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    void deletePetTypeByPetTypeId_WhenPetTypeNotFound_ShouldComplete() {
        try {
            String petTypeId = "non-existent-id";
            when(petTypeRepo.deleteByPetTypeId(petTypeId)).thenReturn(Mono.empty());

            Mono<Void> result = petTypeService.deletePetTypeByPetTypeId(petTypeId);

            StepVerifier.create(result)
                    .verifyComplete();

        } catch (NotFoundException e) {
            fail("Unexpected NotFoundException: " + e.getMessage());
        } catch (InvalidInputException e) {
            fail("Unexpected InvalidInputException: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }


    /*
    @Test
    void insertPetType() {
        PetType petTypEntity = buildPetType();
        Mono<PetType> petTypeMono = Mono.just(petTypEntity);

        when(repo.insert(any(PetType.class))).thenReturn(petTypeMono);

        Mono<PetType> returnedPetType = petTypeService.insertPetType(Mono.just(petTypEntity));

        StepVerifier.create(returnedPetType)
                .consumeNextWith(foundPeType -> {
                    assertEquals(petTypEntity.getId(), foundPeType.getId());
                    assertEquals(petTypEntity.getName(), foundPeType.getName());
                })
                .verifyComplete();
    }

     */

    /*

    @Test
    void getPetTypeById() {

        PetType petType = buildPetType();
        String PET_TYPE_ID = petType.getId();

        when(repo.findPetTypeById(anyInt())).thenReturn(Mono.just(petType));

        Mono<PetType> petTypeTest = petTypeService.getPetTypeById(PET_TYPE_ID);

        StepVerifier
                .create(petTypeTest)
                .consumeNextWith(foundPetType ->{
                    assertEquals(petType.getName(), foundPetType.getName());
                })
                .verifyComplete();
    }

     */


/*
    @Test
    void getAll() {

        PetType petType = buildPetType();

        when(repo.findAll()).thenReturn(Flux.just(petType));
        Flux<PetType> petTypeTest = petTypeService.getAllPetTypes();

        StepVerifier
                .create(petTypeTest)
                .consumeNextWith(foundPetType ->{
                    assertNotNull(foundPetType);
                })
                .verifyComplete();
    }

 */

    private PetType buildPetType() {
        return PetType.builder().id("10").petTypeId("petType-Id-123").name("TestType").petTypeDescription("Mammal").build();
    }



}
