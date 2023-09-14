package com.petclinic.customersservice.presentationlayer;

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
    private WebTestClient client;

    @Autowired
    private OwnerRepo repo;

    Owner ownerEntity = buildOwner();

    String OWNER_ID = ownerEntity.getId();

    @Test
    void deleteOwnerByOwnerId() {
        repo.save(ownerEntity);
        Publisher<Void> setup = repo.deleteById(OWNER_ID);
        StepVerifier.create(setup).expectNextCount(0).verifyComplete();
        client.delete().uri("/owner/" + OWNER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk().expectBody();

    }

    @Test
    void getAllOwners() {
        Publisher<Owner> setup = repo.deleteAll().thenMany(repo.save(ownerEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        client.get().uri("/owner/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(ownerEntity.getId())
                .jsonPath("$[0].firstName").isEqualTo(ownerEntity.getFirstName())
                .jsonPath("$[0].lastName").isEqualTo(ownerEntity.getLastName())
                .jsonPath("$[0].address").isEqualTo(ownerEntity.getAddress())
                .jsonPath("$[0].city").isEqualTo(ownerEntity.getCity())
                .jsonPath("$[0].telephone").isEqualTo(ownerEntity.getTelephone());
                //.jsonPath("$[0].photoId").isEqualTo(ownerEntity.getPhotoId());
    }

    @Test
    void getOwnerByOwnerId() {
        Publisher<Owner> setup = repo.deleteAll().thenMany(repo.save(ownerEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        client.get().uri("/owner/" + OWNER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(ownerEntity.getId())
                .jsonPath("$.firstName").isEqualTo(ownerEntity.getFirstName())
                .jsonPath("$.lastName").isEqualTo(ownerEntity.getLastName())
                .jsonPath("$.address").isEqualTo(ownerEntity.getAddress())
                .jsonPath("$.city").isEqualTo(ownerEntity.getCity())
                .jsonPath("$.telephone").isEqualTo(ownerEntity.getTelephone());
                //.jsonPath("$.photoId").isEqualTo(ownerEntity.getPhotoId());
    }

    @Test
    void updateOwnerByOwnerId() {
        Publisher<Owner> setup = repo.deleteAll().thenMany(repo.save(ownerEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        client.put().uri("/owner/" + OWNER_ID)
                .body(Mono.just(ownerEntity), Owner.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(ownerEntity.getId())
                .jsonPath("$.firstName").isEqualTo(ownerEntity.getFirstName())
                .jsonPath("$.lastName").isEqualTo(ownerEntity.getLastName())
                .jsonPath("$.address").isEqualTo(ownerEntity.getAddress())
                .jsonPath("$.city").isEqualTo(ownerEntity.getCity())
                .jsonPath("$.telephone").isEqualTo(ownerEntity.getTelephone());
                //.jsonPath("$.photoId").isEqualTo(ownerEntity.getPhotoId());

    }

    @Test
    void insertOwner() {
        Publisher<Void> setup = repo.deleteAll();
        StepVerifier.create(setup).expectNextCount(0).verifyComplete();
        client.post().uri("/owner")
                .body(Mono.just(ownerEntity), Owner.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(ownerEntity.getId())
                .jsonPath("$.firstName").isEqualTo(ownerEntity.getFirstName())
                .jsonPath("$.lastName").isEqualTo(ownerEntity.getLastName())
                .jsonPath("$.address").isEqualTo(ownerEntity.getAddress())
                .jsonPath("$.city").isEqualTo(ownerEntity.getCity())
                .jsonPath("$.telephone").isEqualTo(ownerEntity.getTelephone());
                //.jsonPath("$.photoId").isEqualTo(ownerEntity.getPhotoId());
    }



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
