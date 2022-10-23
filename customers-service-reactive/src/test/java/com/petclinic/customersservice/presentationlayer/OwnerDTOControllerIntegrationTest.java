package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.OwnerDTO;
import com.petclinic.customersservice.business.OwnerDTOService;
import com.petclinic.customersservice.business.PetDTO;
import com.petclinic.customersservice.business.PetDTOService;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.data.Photo;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


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
//        OwnerDTO ownerEntity = buildOwnerDTO();
//
//        String OWNER_ID = ownerEntity.getId();
//
//        when(ownerDTOService.getOwnerDTOByOwnerId(anyString())).thenReturn(Mono.just(ownerEntity));
//
//        client.get()
//                .uri("/ownerdto/" + OWNER_ID)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.firstName").isEqualTo(ownerEntity.getFirstName())
//                .jsonPath("$.lastName").isEqualTo(ownerEntity.getLastName())
//                .jsonPath("$.address").isEqualTo(ownerEntity.getAddress())
//                .jsonPath("$.city").isEqualTo(ownerEntity.getCity())
//                .jsonPath("$.telephone").isEqualTo(ownerEntity.getTelephone())
//                .jsonPath("$.photo").isEqualTo(ownerEntity.getPhoto());
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


        private OwnerDTO buildOwnerDTO() {
        return OwnerDTO.builder()
                .id("1")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .telephone("telephone")
                .photo(Photo.builder().id("1").photo("1").name("test").type("test").build())
                .build();
    }
}
