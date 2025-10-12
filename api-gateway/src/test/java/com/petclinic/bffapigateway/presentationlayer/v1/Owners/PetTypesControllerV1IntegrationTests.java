package com.petclinic.bffapigateway.presentationlayer.v1.Owners;

import com.petclinic.bffapigateway.dtos.Pets.PetTypeResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v1.mockservers.MockServerConfigCustomersService;
import com.petclinic.bffapigateway.presentationlayer.v1.mockservers.MockServerConfigAuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.List;
import static com.petclinic.bffapigateway.presentationlayer.v1.mockservers.MockServerConfigAuthService.jwtTokenForValidAdmin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PetTypesControllerV1IntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigCustomersService mockServerConfigCustomersService;
    private MockServerConfigAuthService mockServerConfigAuthService;

    private final String PET_TYPE_PATH = "/api/gateway/owners/petTypes";
    private final String PET_TYPE_COUNT_PATH = "/api/gateway/owners/petTypes/pet-types-filtered-count";

    @BeforeEach
    public void startMockServer() {
        mockServerConfigCustomersService = new MockServerConfigCustomersService();
        mockServerConfigCustomersService.registerGetPetTypesEndpoint();
        mockServerConfigCustomersService.registerGetPetTypesCountEndpoint();

        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
    }

    @AfterEach
    public void stopMockServer() {
        mockServerConfigCustomersService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    PetTypeResponseDTO petType = PetTypeResponseDTO.builder()
            .petTypeId("pt-1")
            .name("Dog")
            .petTypeDescription("Loyal Companion")
            .build();

    @Test
    void whenGetAllPetTypes_WithValidClient_thenReturnResult() {

        Mono<List<PetTypeResponseDTO>> result = webTestClient.get()
                .uri(PET_TYPE_PATH)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(PetTypeResponseDTO.class)
                .getResponseBody()
                .collectList()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(petTypeResponseDTOS -> {
                    assertNotNull(petTypeResponseDTOS);
                    assertThat(petTypeResponseDTOS).isNotEmpty();
                    assertThat(petTypeResponseDTOS.size()).isEqualTo(2);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenGetPetTypeById_withValidId_thenReturnPetType() {
        mockServerConfigCustomersService.registerGetPetTypeByIdEndpoint(petType.getPetTypeId());

        Mono<PetTypeResponseDTO> result = webTestClient.get()
                .uri(PET_TYPE_PATH + "/{petTypeId}", petType.getPetTypeId())
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(PetTypeResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(petTypeResponseDTO -> {
                    assertNotNull(petTypeResponseDTO);
                    assertThat(petTypeResponseDTO.getPetTypeId()).isEqualTo(petType.getPetTypeId());
                    assertThat(petTypeResponseDTO.getName()).isEqualTo(petType.getName());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenGetTotalNumberOfPetTypes_withValidFilters_thenReturnFilteredCount() {
        String countString = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(PET_TYPE_COUNT_PATH)
                        .queryParam("name", "Dog")
                        .queryParam("petTypeId", "pt-1")
                        .build())
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        Mono<String> result = Mono.just(countString);

        StepVerifier
                .create(result)
                .expectNextMatches(s -> {
                    assertNotNull(s);
                    assertThat(s).isEqualTo("2");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenGetAllPetTypes_withServiceFail_thenReturn500() {
        mockServerConfigCustomersService.registerGetPetTypesEndpoint_500();

        webTestClient.get()
                .uri(PET_TYPE_PATH)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void whenGetTotalNumberOfPetTypes_withServiceFail_thenReturn500Error() {
        mockServerConfigCustomersService.registerGetPetTypesCountEndpoint_500();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(PET_TYPE_COUNT_PATH)
                        .queryParam("name", "Dog")
                        .queryParam("petTypeId", "pt-1")
                        .build())
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void whenGetPetTypeById_withNonExistentPetTypeId_thenReturn404NotFound() {
        final String nonExistentId = "non-existent-id-404";

        webTestClient.get()
                .uri(PET_TYPE_PATH + "/{petTypeId}", nonExistentId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}