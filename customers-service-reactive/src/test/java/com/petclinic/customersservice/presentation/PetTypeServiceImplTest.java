package com.petclinic.customersservice.presentation;

import com.petclinic.customersservice.business.PetTypeService;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 27019"})
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
        PetType petTypeEntity = buildPetType();
        Mono<PetType> petTypeMono = Mono.just(petTypeEntity);
        when(repo.insert(any(PetType.class))).thenReturn(petTypeMono);
        StepVerifier.create(petTypeMono)
                .consumeNextWith(foundPetType -> {
                    assertEquals(petTypeEntity.getId(), foundPetType.getId());
                    assertEquals(petTypeEntity.getName(), foundPetType.getName());
                })
                .verifyComplete();

    }

    private PetType buildPetType() {
        return PetType.builder().id(10).name("Test").build();
    }

}