package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import com.petclinic.bffapigateway.utils.Utility;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@WebFluxTest(
        controllers = {OwnerControllerV1.class, PetControllerV1.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtTokenFilter.class, RoleFilter.class, IsUserFilter.class}
        )
)
@AutoConfigureWebTestClient
public class OwnerApiGatewayTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    private CustomersServiceClient customersServiceClient;

    @MockBean
    private AuthServiceClient authServiceClient;
    @MockBean
    private VisitsServiceClient visitsServiceClient;

    @MockBean
    private Utility utility; // satisfies IsUserFilter dependency if accidentally loaded

    private final Date date = new Date();

    @Test
    void getAllOwners_shouldSucceed() {
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-90");
        owner.setFirstName("John");
        owner.setLastName("Johnny");

        when(customersServiceClient.getAllOwners()).thenReturn(Flux.just(owner));

        client.get()
                .uri("/api/gateway/owners")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OwnerResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals("ownerId-90", list.get(0).getOwnerId());
                });
    }

    @Test
    void getAllOwnersByPagination() {
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-09");
        owner.setFirstName("Test");
        owner.setLastName("Test");
        owner.setAddress("Test");
        owner.setCity("Test");
        owner.setProvince("Test");
        owner.setTelephone("Test");

        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(1);

        when(customersServiceClient.getOwnersByPagination(page, size, null, null, null, null, null))
                .thenReturn(Flux.just(owner));

        client.get()
                .uri("/api/gateway/owners/owners-pagination?page=0&size=1")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(OwnerResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals("ownerId-09", list.get(0).getOwnerId());
                });
    }

    @Test
    void getAllOwnersByPagination_pageEmpty_sizeEmpty() {
        when(customersServiceClient.getOwnersByPagination(null, null, null, null, null, null, null))
                .thenReturn(Flux.empty());

        client.get()
                .uri("/api/gateway/owners/owners-pagination")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(OwnerResponseDTO.class)
                .value(list -> assertEquals(0, list.size()));
    }

    @Test
    void getTotalNumberOfOwners() {
        long expectedCount = 0L;
        when(customersServiceClient.getTotalNumberOfOwners()).thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/owners/owners-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(body -> assertEquals(expectedCount, body));
    }

    @Test
    void getTotalNumberOfOwnersWithFilters() {
        long expectedCount = 0L;
        when(customersServiceClient.getTotalNumberOfOwnersWithFilters(null, null, null, null, null))
                .thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/owners/owners-filtered-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(body -> assertEquals(expectedCount, body));
    }


        @Test
        void getOwnerByOwnerId_shouldSucceed() {
            OwnerResponseDTO owner = new OwnerResponseDTO();
            owner.setOwnerId("ownerId-123");
            owner.setFirstName("John");
            owner.setLastName("Johnny");
            owner.setAddress("111 John St");
            owner.setCity("Johnston");
            owner.setProvince("Quebec");
            owner.setTelephone("51451545144");

            when(customersServiceClient.getOwner("ownerId-123"))
                    .thenReturn(Mono.just(owner));

            client.get()
                    .uri("/api/gateway/owners/{ownerId}", owner.getOwnerId())
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(OwnerResponseDTO.class)
                    .value(ownerResponseDTO -> {
                        assertNotNull(ownerResponseDTO);
                        assertEquals(ownerResponseDTO.getOwnerId(), owner.getOwnerId());
                    });
        }

    @Test
    void updateOwner_shouldSucceed() {
        String ownerId = "f470653d-05c5-4c45-b7a0-7d70f003d2ac";
        OwnerRequestDTO updatedOwnerData = new OwnerRequestDTO();
        updatedOwnerData.setOwnerId(ownerId);
        updatedOwnerData.setFirstName("UpdatedFirstName");
        updatedOwnerData.setLastName("UpdatedLastName");

        OwnerResponseDTO updatedOwner = new OwnerResponseDTO();
        updatedOwner.setOwnerId(ownerId);
        updatedOwner.setFirstName(updatedOwnerData.getFirstName());
        updatedOwner.setLastName(updatedOwnerData.getLastName());

        when(customersServiceClient.updateOwner(eq(ownerId), any(Mono.class)))
                .thenReturn(Mono.just(updatedOwner));

        client.put()
                .uri("/api/gateway/owners/" + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updatedOwnerData))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .isEqualTo(updatedOwner);

        Mockito.verify(customersServiceClient, times(1))
                .updateOwner(eq(ownerId), any(Mono.class));
    }
    }


//    @Test
//    void shouldCreatePet() {
//        String ownerId = "ownerId-12345";
//
//        PetResponseDTO pet = new PetResponseDTO();
//        pet.setPetId("30-30-30-30");
//        pet.setOwnerId(ownerId);
//        pet.setName("Fluffy");
//        pet.setBirthDate(date);
//        pet.setPetTypeId("5");
//        pet.setIsActive("true");
//
//        when(customersServiceClient.createPetForOwner(eq(ownerId), any(PetRequestDTO.class)))
//                .thenReturn(Mono.just(pet));
//
//        PetRequestDTO petRequest = new PetRequestDTO();
//        petRequest.setOwnerId(ownerId);
//        petRequest.setName("Fluffy");
//        petRequest.setBirthDate(date);
//        petRequest.setPetTypeId("5");
//        petRequest.setIsActive("true");
//
//        client.post()
//                .uri("/api/gateway/owners/{ownerId}/pets", ownerId)
//                .bodyValue(petRequest)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody()
//                .jsonPath("$.petId").isEqualTo("30-30-30-30")
//                .jsonPath("$.name").isEqualTo("Fluffy");
//
//        verify(customersServiceClient, times(1))
//                .createPetForOwner(eq(ownerId), any(PetRequestDTO.class));
//    }
//
//    @Test
//    void shouldDeletePet() {
//        String petId = "petId-123";
//
//        when(customersServiceClient.deletePetByPetId(petId))
//                .thenReturn(Mono.empty());
//
//        client.delete()
//                .uri("/api/gateway/owners/petTypes/{petTypeId}", petId)
//                .exchange()
//                .expectStatus().isOk();
//
//        verify(customersServiceClient, times(1))
//                .deletePetTypeV2(petId);
//    }
//
//    @Test
//    void shouldPatchPet() {
//        String petId = "petId-123";
//
//        PetRequestDTO petRequestDTO = new PetRequestDTO();
//        petRequestDTO.setPetId(petId);
//        petRequestDTO.setIsActive("true");
//
//        PetResponseDTO expectedPetResponse = new PetResponseDTO();
//        expectedPetResponse.setPetId(petId);
//        expectedPetResponse.setIsActive("true");
//
//        when(customersServiceClient.patchPet(petRequestDTO, petId))
//                .thenReturn(Mono.just(expectedPetResponse));
//
//        client.patch()
//                .uri("/api/gateway/owners/pets/{petId}", petId)
//                .bodyValue(petRequestDTO)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.petId").isEqualTo(petId)
//                .jsonPath("$.isActive").isEqualTo("true");
//
//        verify(customersServiceClient, times(1))
//                .patchPet(petRequestDTO, petId);
//    }


