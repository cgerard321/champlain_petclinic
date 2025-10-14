package com.petclinic.bffapigateway.presentationlayer.v1.Owners;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v1.PetTypeControllerV1;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import com.petclinic.bffapigateway.utils.Utility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebFluxTest(
        controllers = {PetTypeControllerV1.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtTokenFilter.class, RoleFilter.class, IsUserFilter.class}
        )
)
@AutoConfigureWebTestClient
public class PetTypesControllerV1UnitTests {

    @Autowired
    private WebTestClient client;

    @MockBean
    private CustomersServiceClient customersServiceClient;

    @MockBean
    private Utility utility;

    @Test
    void whenGetAllPetTypes_thenReturnPetTypes() {
        PetTypeResponseDTO petType = new PetTypeResponseDTO();
        petType.setPetTypeId("petTypeId-1");
        petType.setName("Dog");
        petType.setPetTypeDescription("Mammal");

        when(customersServiceClient.getAllPetTypes()).thenReturn(Flux.just(petType));

        client.get()
                .uri("/api/gateway/owners/petTypes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PetTypeResponseDTO.class)
                .value(list -> {
                    assertEquals(1, list.size());
                    assertEquals("petTypeId-1", list.get(0).getPetTypeId());
                });

        verify(customersServiceClient, times(1)).getAllPetTypes();
    }

    @Test
    void whenGetAllPetTypes_withEmptyFlux_thenReturnEmptyList() {
        when(customersServiceClient.getAllPetTypes())
                .thenReturn(Flux.empty());

        client.get()
                .uri("/api/gateway/owners/petTypes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PetTypeResponseDTO.class)
                .hasSize(0);

        verify(customersServiceClient, times(1)).getAllPetTypes();
    }

    @Test
    void whenGetPetTypeById_thenReturnPetType() {
        String petTypeId = "petTypeId-123";
        PetTypeResponseDTO petType = new PetTypeResponseDTO();
        petType.setPetTypeId(petTypeId);
        petType.setName("Cat");
        petType.setPetTypeDescription("Mammal");

        when(customersServiceClient.getPetTypeByPetTypeId(petTypeId))
                .thenReturn(Mono.just(petType));

        client.get()
                .uri("/api/gateway/owners/petTypes/{petTypeId}", petTypeId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetTypeResponseDTO.class)
                .value(response -> {
                    assertEquals(petTypeId, response.getPetTypeId());
                    assertEquals("Cat", response.getName());
                });

        verify(customersServiceClient, times(1)).getPetTypeByPetTypeId(petTypeId);
    }

    @Test
    void whenGetPetTypeById_withInvalidId_thenReturnNotFound() {
        String petTypeId = "non-existent-id";

        when(customersServiceClient.getPetTypeByPetTypeId(petTypeId))
                .thenReturn(Mono.empty());

        client.get()
                .uri("/api/gateway/owners/petTypes/{petTypeId}", petTypeId)
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1)).getPetTypeByPetTypeId(petTypeId);
    }

    @Test
    void whenAddPetType_thenReturnCreatedPetType() {
        PetTypeRequestDTO requestDTO = new PetTypeRequestDTO();
        requestDTO.setName("Bird");
        requestDTO.setPetTypeDescription("Flying animal");

        PetTypeResponseDTO responseDTO = new PetTypeResponseDTO();
        responseDTO.setPetTypeId("petTypeId-456");
        responseDTO.setName("Bird");
        responseDTO.setPetTypeDescription("Flying animal");

        when(customersServiceClient.addPetType(any(Mono.class)))
                .thenReturn(Mono.just(responseDTO));

        client.post()
                .uri("/api/gateway/owners/petTypes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PetTypeResponseDTO.class)
                .value(response -> {
                    assertEquals("petTypeId-456", response.getPetTypeId());
                    assertEquals("Bird", response.getName());
                });

        verify(customersServiceClient, times(1)).addPetType(any(Mono.class));
    }

    @Test
    void whenAddPetType_withEmptyMono_thenReturnBadRequest() {
        PetTypeRequestDTO requestDTO = new PetTypeRequestDTO();
        requestDTO.setName("Invalid Pet");
        requestDTO.setPetTypeDescription("Description");

        when(customersServiceClient.addPetType(any(Mono.class)))
                .thenReturn(Mono.empty());

        client.post()
                .uri("/api/gateway/owners/petTypes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isBadRequest();

        verify(customersServiceClient, times(1)).addPetType(any(Mono.class));
    }

    @Test
    void whenUpdatePetType_thenReturnUpdatedPetType() {
        String petTypeId = "petTypeId-123";
        PetTypeRequestDTO requestDTO = new PetTypeRequestDTO();
        requestDTO.setName("Updated Dog");
        requestDTO.setPetTypeDescription("Updated Mammal");

        PetTypeResponseDTO responseDTO = new PetTypeResponseDTO();
        responseDTO.setPetTypeId(petTypeId);
        responseDTO.setName("Updated Dog");
        responseDTO.setPetTypeDescription("Updated Mammal");

        when(customersServiceClient.updatePetType(eq(petTypeId), any(Mono.class)))
                .thenReturn(Mono.just(responseDTO));

        client.put()
                .uri("/api/gateway/owners/petTypes/{petTypeId}", petTypeId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petTypeId").isEqualTo(petTypeId)
                .jsonPath("$.name").isEqualTo("Updated Dog")
                .jsonPath("$.petTypeDescription").isEqualTo("Updated Mammal");

        verify(customersServiceClient, times(1)).updatePetType(eq(petTypeId), any(Mono.class));
    }

    @Test
    void whenUpdatePetType_withInvalidId_thenReturnNotFound() {
        String petTypeId = "non-existent-id";
        PetTypeRequestDTO requestDTO = new PetTypeRequestDTO();
        requestDTO.setName("Updated Dog");
        requestDTO.setPetTypeDescription("Updated Mammal");

        when(customersServiceClient.updatePetType(eq(petTypeId), any(Mono.class)))
                .thenReturn(Mono.empty());

        client.put()
                .uri("/api/gateway/owners/petTypes/{petTypeId}", petTypeId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1)).updatePetType(eq(petTypeId), any(Mono.class));
    }

    @Test
    void whenDeletePetType_thenReturnOk() {
        String petTypeId = "petTypeId-123";

        when(customersServiceClient.deletePetTypeV2(petTypeId))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/owners/petTypes/{petTypeId}", petTypeId)
                .exchange()
                .expectStatus().isOk();

        verify(customersServiceClient, times(1)).deletePetTypeV2(petTypeId);
    }

    @Test
    void whenDeletePetType_withInvalidId_thenReturnOk() {
        String petTypeId = "non-existent-id";

        when(customersServiceClient.deletePetTypeV2(petTypeId))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/owners/petTypes/{petTypeId}", petTypeId)
                .exchange()
                .expectStatus().isOk();

        verify(customersServiceClient, times(1)).deletePetTypeV2(petTypeId);
    }

    @Test
    void whenGetPetTypesByPagination_thenReturnPetTypes() {
        PetTypeResponseDTO petType = new PetTypeResponseDTO();
        petType.setPetTypeId("petTypeId-1");
        petType.setName("Dog");
        petType.setPetTypeDescription("Mammal");

        when(customersServiceClient.getPetTypesByPagination(any(Optional.class), any(Optional.class),
                any(), any(), any()))
                .thenReturn(Flux.just(petType));

        client.get()
                .uri("/api/gateway/owners/petTypes/pet-types-pagination?page=0&size=5")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PetTypeResponseDTO.class)
                .value(list -> {
                    assertEquals(1, list.size());
                    assertEquals("petTypeId-1", list.get(0).getPetTypeId());
                });

        verify(customersServiceClient, times(1)).getPetTypesByPagination(any(Optional.class),
                any(Optional.class), any(), any(), any());
    }

    @Test
    void whenGetPetTypesByPagination_withAllFilters_thenReturnValidFlux() {
        int page = 0;
        int size = 1;
        String petTypeIdParam = "petTypeId-1";
        String nameParam = "Dog";
        String descriptionParam = "Mammal";

        PetTypeResponseDTO petType = new PetTypeResponseDTO();
        petType.setPetTypeId(petTypeIdParam);
        petType.setName(nameParam);
        petType.setPetTypeDescription(descriptionParam);

        when(customersServiceClient.getPetTypesByPagination(
                eq(Optional.of(page)), eq(Optional.of(size)), eq(petTypeIdParam), eq(nameParam), eq(descriptionParam)))
                .thenReturn(Flux.just(petType));

        client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/gateway/owners/petTypes/pet-types-pagination")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .queryParam("petTypeId", petTypeIdParam)
                        .queryParam("name", nameParam)
                        .queryParam("description", descriptionParam)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PetTypeResponseDTO.class)
                .hasSize(1);

        verify(customersServiceClient, times(1)).getPetTypesByPagination(
                any(Optional.class), any(Optional.class), eq(petTypeIdParam), eq(nameParam), eq(descriptionParam));
    }

    @Test
    void whenGetPetTypesByPagination_withNoMatchingResults_thenReturnEmptyList() {
        String nameParam = "Unmatchable Pet Name";

        when(customersServiceClient.getPetTypesByPagination(
                any(Optional.class), any(Optional.class), any(), eq(nameParam), any()))
                .thenReturn(Flux.empty());

        client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/gateway/owners/petTypes/pet-types-pagination")
                        .queryParam("name", nameParam)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PetTypeResponseDTO.class)
                .hasSize(0);

        verify(customersServiceClient, times(1)).getPetTypesByPagination(
                any(Optional.class), any(Optional.class), any(), eq(nameParam), any());
    }

    @Test
    void whenGetTotalNumberOfPetTypes_thenReturnCount() {
        Long expectedCount = 10L;
        when(customersServiceClient.getTotalNumberOfPetTypes())
                .thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/owners/petTypes/pet-types-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(count -> assertEquals(expectedCount, count));

        verify(customersServiceClient, times(1)).getTotalNumberOfPetTypes();
    }

    @Test
    void whenGetTotalNumberOfPetTypesWithFilters_thenReturnCount() {
        Long expectedCount = 5L;
        when(customersServiceClient.getTotalNumberOfPetTypesWithFilters(any(), any(), any()))
                .thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/owners/petTypes/pet-types-filtered-count?name=Dog")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(count -> assertEquals(expectedCount, count));

        verify(customersServiceClient, times(1)).getTotalNumberOfPetTypesWithFilters(any(), any(), any());
    }

    @Test
    void whenGetTotalNumberOfPetTypesWithFilters_withNoFilters_thenReturnTotalCount() {
        Long expectedCount = 10L;

        when(customersServiceClient.getTotalNumberOfPetTypesWithFilters(
                eq(null), eq(null), eq(null)))
                .thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/owners/petTypes/pet-types-filtered-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .isEqualTo(expectedCount);

        verify(customersServiceClient, times(1)).getTotalNumberOfPetTypesWithFilters(
                eq(null), eq(null), eq(null));
    }
}