package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.OwnerDTOService;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;


@SpringBootTest
@AutoConfigureWebTestClient
public class OwnerDTOControllerIntegrationTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    private OwnerDTOService ownerDTOService;

    @Autowired
    private OwnerRepo ownerRepo;

//    @Test
//    void returnOwnerDTOByOwnerId() {
//
//        Owner ownerEntity = buildOwner();
//
//        String OWNER_ID = ownerEntity.getId();
//
//        Publisher<Owner> setup = ownerRepo.deleteAll().thenMany(ownerRepo.insert(ownerEntity));
//
//        StepVerifier
//                .create(setup)
//                .expectNextCount(1)
//                .verifyComplete();
//
//        client.get()
//                .uri("/ownerdto/" + OWNER_ID)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.firstName").isEqualTo(ownerEntity.getFirstName())
//                .jsonPath("$.lastName").isEqualTo(ownerEntity.getLastName())
//                .jsonPath("$.address").isEqualTo(ownerEntity.getAddress())
//                .jsonPath("$.city").isEqualTo(ownerEntity.getCity())
//                .jsonPath("$.telephone").isEqualTo(ownerEntity.getTelephone());
//  //              .jsonPath("$.photo").isEqualTo(ownerEntity.getPhoto());
//    }

//    private Owner buildOwner() {
//        return Owner.builder()
//                .id("1")
//                .firstName("FirstName")
//                .lastName("LastName")
//                .address("Test address")
//                .city("test city")
//                .telephone("telephone")
//                .photoId("1")
//                .build();
//    }
//
//
//        private OwnerResponseDTO buildOwnerDTO() {
//        return OwnerResponseDTO.builder()
//                .id("1")
//                .firstName("FirstName")
//                .lastName("LastName")
//                .address("Test address")
//                .city("test city")
//                .telephone("telephone")
//               // .photo(Photo.builder().id("1").photo("1").name("test").type("test").build())
//                .build();
//    }
}
