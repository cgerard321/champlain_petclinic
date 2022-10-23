package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
<<<<<<< HEAD

import static org.mockito.ArgumentMatchers.*;
=======
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
>>>>>>> main
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 27020"})
@AutoConfigureWebTestClient
class OwnerServiceImplTest {

    @MockBean
    private OwnerRepo repo;

    @Autowired
    private OwnerService ownerService;

    @Test
    void insertOwner() {
        Owner ownerEntity = buildOwner();
        Mono<Owner> ownerMono = Mono.just(ownerEntity);
        when(repo.insert(any(Owner.class))).thenReturn(ownerMono);
        Mono<Owner> returnedOwner = ownerService.insertOwner(Mono.just(ownerEntity));
        StepVerifier.create(returnedOwner).consumeNextWith(foundOwner -> {
            assertEquals(ownerEntity.getId(), foundOwner.getId());
            assertEquals(ownerEntity.getFirstName(), foundOwner.getFirstName());
            assertEquals(ownerEntity.getLastName(), foundOwner.getLastName());
            assertEquals(ownerEntity.getAddress(), foundOwner.getAddress());
            assertEquals(ownerEntity.getCity(), foundOwner.getCity());
            assertEquals(ownerEntity.getTelephone(), foundOwner.getTelephone());
            assertEquals(ownerEntity.getPhotoId(), foundOwner.getPhotoId());
        })
                .verifyComplete();
    }

<<<<<<< HEAD
//     @Test
//     void getOwnerByOwnerId() {
//        Owner ownerEntity = buildOwner();
//        String OWNER_ID = ownerEntity.getId();
//        when(repo.findById(anyString())).thenReturn(Mono.just(ownerEntity));
//        Mono<Owner> ownerMono = ownerService.getOwnerByOwnerId(OWNER_ID);
//        StepVerifier
//                .create(ownerMono)
//                .consumeNextWith(foundOwner -> {
//                    assertEquals(ownerEntity.getId(), foundOwner.getId());
//                    assertEquals(ownerEntity.getFirstName(), foundOwner.getFirstName());
//                    assertEquals(ownerEntity.getLastName(), foundOwner.getLastName());
//                    assertEquals(ownerEntity.getAddress(), foundOwner.getAddress());
//                    assertEquals(ownerEntity.getCity(), foundOwner.getCity());
//                    assertEquals(ownerEntity.getTelephone(), foundOwner.getTelephone());
//                    assertEquals(ownerEntity.getPhotoId(), foundOwner.getPhotoId());
//                })
//                .verifyComplete();
//     }

     @Test
     void deleteOwnerByOwnerId() {
        Owner ownerEntity = buildOwner();
        String OWNER_ID = ownerEntity.getId();
        when(repo.deleteById(anyString())).thenReturn(Mono.empty());
        Mono<Void> deleteObj = ownerService.deleteOwner(OWNER_ID);
        StepVerifier
                .create(deleteObj)
                .expectNextCount(0)
                .verifyComplete();
     }
=======
    @Test
    public void deleteOwner() {

        Owner owner = buildOwner();
        int OWNER_ID = owner.getId();

        when(repo.deleteById(anyInt())).thenReturn(Mono.empty());

        Mono<Void> ownerDelete = ownerService.deleteOwner(OWNER_ID);

        StepVerifier
                .create(ownerDelete)
                .expectNextCount(0)
                .verifyComplete();
    }

>>>>>>> main
    @Test
    public void deleteOwnerNotFound() {

        Owner owner = buildOwner();
<<<<<<< HEAD
        String OWNER_ID = "00";

        when(repo.deleteById(anyString())).thenReturn(Mono.empty());
=======
        int OWNER_ID = 00;

        when(repo.deleteById(anyInt())).thenReturn(Mono.empty());
>>>>>>> main

        Mono<Void> ownerDelete = ownerService.deleteOwner(OWNER_ID);

        StepVerifier
                .create(ownerDelete)
                .expectNextCount(1)
                .expectError();
    }

<<<<<<< HEAD

=======
>>>>>>> main
    private Owner buildOwner() {
        return Owner.builder()
                .id("55")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .telephone("telephone")
                .photoId("1")
                .build();
    }

}
