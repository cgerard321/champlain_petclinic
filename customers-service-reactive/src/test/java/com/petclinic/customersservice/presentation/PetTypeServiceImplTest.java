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
import reactor.core.publisher.Mono;

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
        PetType petTypEntity = buildPetType();
        Mono<PetType> petTypeMono = Mono.just(petTypEntity);

    }

    private PetType buildPetType() {
        return PetType.builder().id(5).name("Dog").build();
    }

}
