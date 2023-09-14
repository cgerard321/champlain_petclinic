package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.data.Photo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
class OwnerDTOServiceImplTest {

    @Autowired
    private WebTestClient webTestClient;

    /*@Autowired
    private OwnerDTOService ownerDTOService;*/

    @Autowired
    private OwnerService ownerService;

    @MockBean
    private OwnerRepo ownerRepo;

//    @Test
//    void insertOwnerDTO() {
//
//        Owner owner = buildOwner();
//
//        Mono<Owner> ownerMono = Mono.just(owner);
//
//        OwnerDTO ownerDTO = buildOwnerDTO();
//
//        when(ownerRepo.insert(any(Owner.class))).thenReturn(ownerMono);
//
//        Mono<OwnerDTO> returnedOwner = ownerService.insertOwner(Mono.just(ownerDTO));
//
//        StepVerifier
//                .create(returnedOwner)
//                .consumeNextWith(foundOwner ->{
//
//                })
//                .verifyComplete();
//    }

//        @Test
//        void GetOwnerDTOByOwnerID() {
//            Owner ownerEntity = buildOwner();
//
//            String Owner_ID = ownerEntity.getId();
//
//            when(ownerRepo.findById(anyString())).thenReturn(Mono.just(ownerEntity));
//
//            Mono<OwnerDTO> ownerDTOMono = ownerDTOService.getOwnerDTOByOwnerId(Owner_ID);
//
//            StepVerifier.create(ownerDTOMono)
//                    .consumeNextWith(foundOwner ->{
//                        assertEquals(ownerEntity.getFirstName(), foundOwner.getFirstName());
//                        assertEquals(ownerEntity.getLastName(), foundOwner.getLastName());
//                        assertEquals(ownerEntity.getAddress(), foundOwner.getAddress());
//                        assertEquals(ownerEntity.getCity(), foundOwner.getCity());
//                        assertEquals(ownerEntity.getTelephone(), foundOwner.getTelephone());
//                        assertEquals(ownerEntity.getPhotoId(), foundOwner.getPhotoId());
//                    })
//                    .verifyComplete();
//
//    }

    private Owner buildOwner() {
        return Owner.builder()
                .id("9")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .telephone("telephone")
                //.photoId("1")
                .build();
    }

}