package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.web.util.UriComponentsBuilder;
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

    @MockBean
    private FilesServiceClient filesServiceClient;

    private Owner buildOwner() {
        return Owner.builder()
                .id("55")
                .ownerId("ownerId-123")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .province("test province")
                .telephone("telephone")
                .build();
    }

    private Owner buildOwner2() {
        return Owner.builder()
                .id("56")
                .ownerId("ownerId-456")
                .firstName("FirstName2")
                .lastName("LastName2")
                .address("Test address2")
                .city("test city2")
                .province("test province2")
                .telephone("telephone2")
                .build();
    }

    private Owner buildOwner3(String firstName, String ownerId) {
        return Owner.builder()
                .ownerId(ownerId)
                .firstName(firstName)
                .lastName("Doe")
                .address("123 Main St")
                .city("Anytown")
                .province("CA")
                .telephone("555-555-5555")
                .build();
    }

    Owner ownerEntity = buildOwner();

    Owner ownerEntity2 = buildOwner2();

    String OWNER_ID = ownerEntity.getId();

    String PUBLIC_OWNER_ID = ownerEntity.getOwnerId();

    Owner owner1 = buildOwner3("Billy","ownerId_1");

    @Test
    void deleteOwnerByOwnerId() {

        StepVerifier.create(repo.deleteAll()).verifyComplete();

        Owner ownerEntity = Owner.builder()
                .id("9")
                .ownerId("a6e0e5b0-5f60-45f0-8ac7-becd8b330486")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .province("province")
                .telephone("telephone")
                .build();

        StepVerifier.create(repo.save(ownerEntity))
                .expectNextMatches(saved -> saved.getOwnerId().equals("a6e0e5b0-5f60-45f0-8ac7-becd8b330486"))
                .verifyComplete();

        client.delete().uri("/owners/a6e0e5b0-5f60-45f0-8ac7-becd8b330486")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }

    @Test
    void deleteNonExistentOwnerByOwnerId() {

        StepVerifier.create(repo.deleteAll()).verifyComplete();


        String nonExistentOwnerId = "a6e0e5b0-5f60-45f0-8ac7-becd8b330486";

        client.delete().uri("/owners/" + nonExistentOwnerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Course id not found: " + nonExistentOwnerId);
    }



    @Test
    void getTotalNumberOfOwners(){
        Owner owner1 = Owner.builder()
                .ownerId("ownerId-11")
                .firstName("FirstName1")
                .lastName("LastName1")
                .address("Test address1")
                .city("test city1")
                .province("province1")
                .telephone("telephone1")
                .build();

        StepVerifier.create(repo.deleteAll().thenMany(repo.save(owner1))).expectNextCount(1).verifyComplete();

        client.get()
                .uri("/owners/owners-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(total -> {
                    assertNotNull(total);
                    assertEquals(1L, total);
                });

    }

    @Test
    void getOwnersPagination() {

        Owner owner1 = Owner.builder()
                .ownerId("ownerId-11")
                .firstName("FirstName1")
                .lastName("LastName1")
                .address("Test address1")
                .city("test city1")
                .province("province1")
                .telephone("telephone1")
                .build();

        int page = 0;
        int size = 1;

        StepVerifier.create(repo.deleteAll().thenMany(repo.save(owner1))).expectNextCount(1).verifyComplete();
        StepVerifier.create(repo.save(owner1)).expectNextCount(1).verifyComplete();

        client.get()
                .uri("/owners/owners-pagination?page="+page+"&size="+size)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange().expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type","text/event-stream;charset=UTF-8")
                .expectBodyList(OwnerResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(size,list.size());
                });

    }

    @Test
    void getTotalNumberOfOwnersWithFilters1_shouldSucceed(){

        String firstName = "FirstName1";
        String city = "test city1";


        Owner owner1 = Owner.builder()
                .ownerId("ownerId-1")
                .firstName("FirstName1")
                .lastName("LastName1")
                .address("Test address1")
                .city("test city1")
                .province("province1")
                .telephone("telephone1")
                .photoId(null)
                .build();

        StepVerifier.create(repo.deleteAll().thenMany(repo.save(owner1))).expectNextCount(1).verifyComplete();

        client.get()
                .uri("/owners/owners-filtered-count?&firstName="+firstName+"&city="+city)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(total -> {
                    assertNotNull(total);
                    assertEquals(1L, total);
                });
    }

    @Test
    void getTotalNumberOfOwnersWithFilters2_shouldSucceed(){

        String firstName = "FirstName2";
        String ownerId = "ownerId-2";


        Owner owner1 = Owner.builder()
                .ownerId("ownerId-2")
                .firstName("FirstName2")
                .lastName("LastName2")
                .address("Test address2")
                .city("test city2")
                .province("province2")
                .telephone("telephone2")
                .build();

        StepVerifier.create(repo.deleteAll().thenMany(repo.save(owner1))).expectNextCount(1).verifyComplete();

        client.get()
                .uri("/owners/owners-filtered-count?&firstName="+firstName+"&ownerId="+ownerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(total -> {
                    assertNotNull(total);
                    assertEquals(1L, total);
                });
    }

    @Test
    void getTotalNumberOfOwnersWithFilters3_shouldSucceed(){

        String firstName = "FirstName3";
        String ownerId = "ownerId-3";
        String lastname = "LastName3";
        String city = "test city3";
        String telephone = "telephone3";


        Owner owner1 = Owner.builder()
                .ownerId("ownerId-3")
                .firstName("FirstName3")
                .lastName("LastName3")
                .address("Test address3")
                .city("test city3")
                .province("province3")
                .telephone("telephone3")
                .build();

        StepVerifier.create(repo.deleteAll().thenMany(repo.save(owner1))).expectNextCount(1).verifyComplete();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/owners/owners-filtered-count");

        builder.queryParam("ownerId", ownerId);
        builder.queryParam("firstName", firstName);
        builder.queryParam("lastName",lastname);
        builder.queryParam("city", city);
        builder.queryParam("phoneNumber", telephone);


        client.get()
                .uri(builder.build().toUri())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(total -> {
                    assertNotNull(total);
                    assertEquals(1L, total);
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
                    assertEquals(ownerResponseDTO.getProvince(),ownerEntity.getProvince());
                    assertEquals(ownerResponseDTO.getTelephone(),ownerEntity.getTelephone());
                });

    }

    @Test
    void updateOwnerByOwnerId() {
        // Setup a unique owner for this test
        String testOwnerId = "update-test-id-123";
        Owner existingOwner = buildOwner3("OldFirst", testOwnerId);
        existingOwner.setId("1");

        // 1. Save the existing owner
        Publisher<Owner> setup = repo.deleteAll().then(repo.save(existingOwner));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();

        // 2. Prepare the update DTO (assuming a full request DTO is needed)
        OwnerRequestDTO updateDTO = new OwnerRequestDTO();
        updateDTO.setFirstName("NewFirstName");
        updateDTO.setLastName("NewLastName");
        updateDTO.setAddress("New Address");
        updateDTO.setCity("New City");
        updateDTO.setProvince("New Province");
        updateDTO.setTelephone("999-999-9999");
        // photoId is left null

        // 3. Make the PUT request
        client.put()
                .uri("/owners/" + testOwnerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateDTO), OwnerRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                // 4. Assert the response contents
                .expectBody(OwnerResponseDTO.class)
                .value(responseDTO -> {
                    assertNotNull(responseDTO);
                    assertEquals(testOwnerId, responseDTO.getOwnerId());
                    assertEquals("NewFirstName", responseDTO.getFirstName());
                    assertEquals("New City", responseDTO.getCity());
                });

        // 5. Verify the update persisted (optional but robust)
        Mono<Owner> checkOwner = repo.findOwnerByOwnerId(testOwnerId);
        StepVerifier.create(checkOwner)
                .expectNextMatches(owner ->
                        owner.getFirstName().equals("NewFirstName") &&
                                owner.getCity().equals("New City")
                )
                .verifyComplete();
    }
}
