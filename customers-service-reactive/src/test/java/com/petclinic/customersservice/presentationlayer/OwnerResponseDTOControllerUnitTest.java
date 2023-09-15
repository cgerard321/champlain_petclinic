package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.OwnerDTOService;
import com.petclinic.customersservice.data.Photo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

//@WebFluxTest(controllers = OwnerDTOController.class)
class OwnerResponseDTOControllerUnitTest {


    /*private OwnerResponseDTO dto = buildOwnerDTO();
    private final String OWNER_ID = dto.getId();*/

    @Autowired
    private WebTestClient client;

    @MockBean
    private OwnerDTOService ownerDTOService;

    /*@Test
    void getOwnerDTOByOwnerId() {
        when(ownerDTOService.getOwnerDTOByOwnerId(anyString())).thenReturn(Mono.just(dto));

        client
                .get()
                .uri("/ownerdto/" + OWNER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.firstName").isEqualTo(dto.getFirstName())
                .jsonPath("$.lastName").isEqualTo(dto.getLastName())
                .jsonPath("$.address").isEqualTo(dto.getAddress())
                .jsonPath("$.city").isEqualTo(dto.getCity())
                .jsonPath("$.telephone").isEqualTo(dto.getTelephone())
                .jsonPath("$.photo").isEqualTo(dto.getPhoto());

        Mockito.verify(ownerDTOService, times(1)).getOwnerDTOByOwnerId(OWNER_ID);
    }

    private OwnerResponseDTO buildOwnerDTO() {
        return OwnerResponseDTO.builder()
                .id("1")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .telephone("telephone")
                .photo(Photo.builder().id("1").photo("1").name("test").type("test").build())
                .build();
    }*/

}