package com.petclinic.customersservice.business;

import com.petclinic.customersservice.business.PetTypeService;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import org.springframework.beans.BeanUtils;
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
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 27017"})
@AutoConfigureWebTestClient
class PetTypeServiceImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PetTypeService petTypeService;

    @MockBean
    private PetTypeRepo repo;

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

    @Test
    void getAll() {

        PetType petType = buildPetType();

        when(repo.findAll()).thenReturn(Flux.just(petType));

        Flux<PetType> petTypeTest = petTypeService.getAll();

        StepVerifier
                .create(petTypeTest)
                .consumeNextWith(foundPetType ->{
                    assertNotNull(foundPetType);
                })
                .verifyComplete();
    }

    @Test
    void deletePetTypeByID(){
        PetType petType = buildPetType();

        int id = petType.getId();

        when(repo.deletePetTypeById(anyInt())).thenReturn(Mono.empty());

        Mono<Void> deleteObj = petTypeService.deletePetType(id);

        StepVerifier.create(deleteObj)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void updatePetType(){

        PetType pet = buildPetType();
        int id = pet.getId();
        PetType pet2 = buildPetType();
        pet2.setName("UpdatedName");

        PetType updatedPet = new PetType();
        BeanUtils.copyProperties(pet, updatedPet);
        updatedPet.setName(pet2.getName());

        when(repo.findPetTypesById(anyInt())).thenReturn(Mono.just(pet));
        when(repo.save(any(PetType.class))).thenReturn(Mono.just(updatedPet));

        Mono<PetType> pet3 = petTypeService.updatePetType(id,Mono.just(pet2));

        StepVerifier.create(pet3)
                .consumeNextWith(foundPet -> {
                    assertNotEquals(pet.getName(), foundPet.getName());
                })
                .verifyComplete();
    }



    private PetType buildPetType() {
        return PetType.builder().id(10).name("TestType").build();
    }

}
