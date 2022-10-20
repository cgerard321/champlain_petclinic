package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import com.petclinic.customersservice.data.Photo;
import com.petclinic.customersservice.data.PhotoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 27019"})
@AutoConfigureWebTestClient
class PetServiceImplTest {

    @MockBean
    private PetRepo repo;

    @Autowired
    private PetService petService;

//    @Test
//    void insertPet() {
//        Pet petEntity = buildPet();
//        Mono<Pet> petMono = Mono.just(petEntity);
//        when(repo.insert(any(Pet.class))).thenReturn(petMono);
//        Mono<Pet> returnedPet = petService.insertPet(Mono.just(petEntity));
//        StepVerifier
//                .create(returnedPet)
//                .consumeNextWith(foundPet -> {
//                    assertEquals(petEntity.getId(), foundPet.getId());
//                    assertEquals(petEntity.getName(), foundPet.getName());
//                    assertEquals(petEntity.getPetTypeId(), foundPet.getPetTypeId());
//                    assertEquals(petEntity.getPhotoId(), foundPet.getPhotoId());
//                    assertEquals(petEntity.getOwnerId(), foundPet.getOwnerId());
//                    assertEquals(petEntity.getBirthDate(), foundPet.getBirthDate());
//                })
//                .verifyComplete();
//    }
//
//    Date date = new Date(20221010);
//
//    private Pet buildPet() {
//        return Pet.builder()
//                .id(55)
//                .name("Test Pet")
//                .petTypeId(5)
//                .photoId(3)
//                .birthDate(date)
//                .ownerId(4)
//                .build();
//    }

}
