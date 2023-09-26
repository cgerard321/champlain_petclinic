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
import static org.junit.jupiter.api.Assertions.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureWebTestClient
class OwnerControllerIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private OwnerRepo repo;

    Owner ownerEntity = buildOwner();

    Owner ownerEntity2 = buildOwner2();

    String OWNER_ID = ownerEntity.getId();

    String PUBLIC_OWNER_ID = ownerEntity.getOwnerId();

    Owner owner1 = buildOwner3("Billy","ownerId_1");



    @Test
    void deleteOwnerByOwnerId() {
        repo.save(ownerEntity);
        Publisher<Void> setup = repo.deleteById(OWNER_ID);
        StepVerifier.create(setup).expectNextCount(0).verifyComplete();
        client.delete().uri("/owners/" + OWNER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk().expectBody();

    }

    @Test
    void getAllOwners() {
        StepVerifier
                .create(repo.save(ownerEntity2))
                .expectNext(ownerEntity2)
                .verifyComplete();

        client.get()
                .uri("/owners")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange().expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type","text/event-stream;charset=UTF-8")
                .expectBodyList(OwnerResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(11,list.size());
                });

    }

    @Test
    void getOwnerByOwnerId() {
        Publisher<Owner> setup = repo.deleteAll().thenMany(repo.save(ownerEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        client.get().uri("/owners/" + PUBLIC_OWNER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .value(ownerResponseDTO -> {
                    assertNotNull(ownerResponseDTO);
                    assertEquals(ownerResponseDTO.getOwnerId(),ownerEntity.getOwnerId());
                    assertEquals(ownerResponseDTO.getFirstName(),ownerEntity.getFirstName());
                    assertEquals(ownerResponseDTO.getLastName(),ownerEntity.getLastName());
                    assertEquals(ownerResponseDTO.getAddress(),ownerEntity.getAddress());
                    assertEquals(ownerResponseDTO.getCity(),ownerEntity.getCity());
                    assertEquals(ownerResponseDTO.getTelephone(),ownerEntity.getTelephone());
                });
//                .jsonPath("$.id").isEqualTo(ownerEntity.getId())
//                .jsonPath("$.firstName").isEqualTo(ownerEntity.getFirstName())
//                .jsonPath("$.lastName").isEqualTo(ownerEntity.getLastName())
//                .jsonPath("$.address").isEqualTo(ownerEntity.getAddress())
//                .jsonPath("$.city").isEqualTo(ownerEntity.getCity())
//                .jsonPath("$.telephone").isEqualTo(ownerEntity.getTelephone());
//                //.jsonPath("$.photoId").isEqualTo(ownerEntity.getPhotoId());
    }

//    @Test
//    void updateOwnerByOwnerId() {
//        Publisher<Owner> setup = repo.deleteAll().thenMany(repo.save(ownerEntity));
//        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
//        client.put().uri("/owners/" + OWNER_ID)
//                .body(Mono.just(ownerEntity), Owner.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange().expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.id").isEqualTo(ownerEntity.getId())
//                .jsonPath("$.firstName").isEqualTo(ownerEntity.getFirstName())
//                .jsonPath("$.lastName").isEqualTo(ownerEntity.getLastName())
//                .jsonPath("$.address").isEqualTo(ownerEntity.getAddress())
//                .jsonPath("$.city").isEqualTo(ownerEntity.getCity())
//                .jsonPath("$.telephone").isEqualTo(ownerEntity.getTelephone());
//                //.jsonPath("$.photoId").isEqualTo(ownerEntity.getPhotoId());
//
//    }

    @Test
    void updateOwnerByOwnerId() {
        // Ensure that an owner with valid data exists in the repository
        repo.save(ownerEntity).block();

        // Create an updated owner object
        Owner updatedOwner = Owner.builder()
                .ownerId(PUBLIC_OWNER_ID) // Set the same ownerId
                .firstName("UpdatedFirstName")
                .lastName("UpdatedLastName")
                .address("Updated Address")
                .city("Updated City")
                .telephone("Updated Telephone")
                //.photoId("Updated PhotoId")
                .build();

        // Send a PUT request to update the owner
        client.put()
                .uri("/owners/" + PUBLIC_OWNER_ID) // Use the ownerId for the update
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedOwner)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.ownerId").isEqualTo(updatedOwner.getOwnerId()) // Verify the updated ID
                .jsonPath("$.firstName").isEqualTo(updatedOwner.getFirstName())
                .jsonPath("$.lastName").isEqualTo(updatedOwner.getLastName())
                .jsonPath("$.address").isEqualTo(updatedOwner.getAddress())
                .jsonPath("$.city").isEqualTo(updatedOwner.getCity())
                .jsonPath("$.telephone").isEqualTo(updatedOwner.getTelephone());
        //.jsonPath("$.photoId").isEqualTo(updatedOwner.getPhotoId());
    }

    @Test
    void insertOwner() {
        Publisher<Void> setup = repo.deleteAll();
        StepVerifier.create(setup).expectNextCount(0).verifyComplete();
        client.post().uri("/owners")
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
                .ownerId("ownerId-123")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .telephone("telephone")
                //.photoId("1")
                .build();
    }

    private Owner buildOwner2() {
        return Owner.builder()
                .id("67")
                .ownerId("ownerId-1234")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .telephone("telephone")
                //.photoId("1")
                .build();
    }

    private Owner buildOwner3(String firstName, String ownerId) {
        return Owner.builder()
                .ownerId("ownerId-1234")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .telephone("telephone")
                //.photoId("1")
                .build();
    }



}
