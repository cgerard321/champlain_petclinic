package com.petclinic.customersservice.presentation;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@SpringBootTest
@AutoConfigureWebTestClient
class OwnerControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OwnerRepo ownerRepo;



    @Test
    void deleteOwnerByOwnerId(){

        Owner owner = buildOwner();
        ownerRepo.save(owner);

        Publisher<Void> setup = ownerRepo.deleteById(buildOwner().getId());

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        webTestClient.delete()
                .uri("/owners/" + owner.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }


    private Owner buildOwner() {
        return Owner.builder()
                .id(55)
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .telephone("telephone")
                .photoId(1)
                .build();
    }

}