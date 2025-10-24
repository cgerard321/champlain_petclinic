package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigCustomersService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PetControllerIntegrationTest {

    private final String VALID_OWNER_ID = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
    private final String VALID_PET_ID = "53163352-8398-4513-bdff-b7715c056d1d";
    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigCustomersService mockServerConfigCustomersService;
    private MockServerConfigAuthService mockServerConfigAuthService;

    @BeforeAll
    public void startMockServer() {
        mockServerConfigCustomersService = new MockServerConfigCustomersService();
        mockServerConfigCustomersService.registerDeletePetEndpoint();
        mockServerConfigCustomersService.registerGetOwnerByIdEndpoint();
        mockServerConfigCustomersService.registerUpdatePetEndpoint(VALID_PET_ID, VALID_OWNER_ID, "UpdatedBuddy");
        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerValidateTokenForVetEndpoint();
        mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint();
        mockServerConfigCustomersService.registerGetPetByIdEndpoint();
    }

    @AfterAll
    public void stopMockServer() {
        mockServerConfigCustomersService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    @Test
    void whenGetPetForOwner_asOwner_withValidIds_thenReturnPet() {
        final String VALID_PET_ID = "53163352-8398-4513-bdff-b7715c056d1d";
        final String OWNER_ID_OF_PET = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";

        mockServerConfigCustomersService.registerGetPetByIdEndpoint();

        Mono<PetResponseDTO> result = webTestClient.get()
                .uri("/api/v2/gateway/pets/owners/{ownerId}/pets/{petId}", OWNER_ID_OF_PET, VALID_PET_ID)
                .cookie("Bearer", jwtTokenForValidOwnerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(PetResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(petResponseDTO -> {
                    assertNotNull(petResponseDTO);
                    assertThat(petResponseDTO.getPetId()).isEqualTo(VALID_PET_ID);
                    assertThat(petResponseDTO.getOwnerId()).isEqualTo(OWNER_ID_OF_PET);
                    return true;
                })
                .verifyComplete();
    }

}