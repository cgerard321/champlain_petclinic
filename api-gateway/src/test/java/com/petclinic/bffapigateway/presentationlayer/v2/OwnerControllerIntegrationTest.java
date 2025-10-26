package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigCustomersService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OwnerControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigCustomersService mockServerConfigCustomersService;

    private MockServerConfigAuthService mockServerConfigAuthService;

    @BeforeAll
    public void startMockServer() {
        mockServerConfigCustomersService = new MockServerConfigCustomersService();
        mockServerConfigCustomersService.registerUpdateOwnerEndpoint();
        mockServerConfigCustomersService.registerAddOwnerEndpoint();
        mockServerConfigCustomersService.registerGetAllOwnersEndpoint();
        mockServerConfigCustomersService.registerDeleteOwnerEndpoint();
        mockServerConfigCustomersService.registerDeleteOwnerEmptyResponseEndpoint();
        mockServerConfigCustomersService.registerGetOwnerByIdEndpoint();

        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerValidateTokenForVetEndpoint();

    }

    @AfterAll
    public void stopMockServer() {
        mockServerConfigCustomersService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }
    
    @Test
    public void whenDeleteOwner_asAdmin_thenReturnOwnerResponse() {
        // Mock data to simulate the OwnerResponseDTO
        OwnerResponseDTO expectedOwner = new OwnerResponseDTO();
        expectedOwner.setOwnerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
        expectedOwner.setFirstName("Betty");
        expectedOwner.setLastName("Davis");
        expectedOwner.setAddress("638 Cardinal Ave.");
        expectedOwner.setCity("Sun Prairie");
        expectedOwner.setProvince("Quebec");
        expectedOwner.setTelephone("6085551749");

        // Perform the DELETE request and expect OwnerResponseDTO in the body
        webTestClient.delete()
                .uri("/api/v2/gateway/owners/{ownerId}", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .cookie("Bearer", "valid-test-token-for-valid-admin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Now we expect a 200 OK response, not 204 NO_CONTENT
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .value(ownerResponse -> {
                    assertThat(ownerResponse.getOwnerId()).isEqualTo(expectedOwner.getOwnerId());
                    assertThat(ownerResponse.getFirstName()).isEqualTo(expectedOwner.getFirstName());
                    assertThat(ownerResponse.getLastName()).isEqualTo(expectedOwner.getLastName());
                    assertThat(ownerResponse.getAddress()).isEqualTo(expectedOwner.getAddress());
                    assertThat(ownerResponse.getCity()).isEqualTo(expectedOwner.getCity());
                    assertThat(ownerResponse.getProvince()).isEqualTo(expectedOwner.getProvince());
                    assertThat(ownerResponse.getTelephone()).isEqualTo(expectedOwner.getTelephone());
                });
    }


    @Test
    void whenDeleteOwner_withInvalidOwnerIdLength_thenReturnUnprocessableEntity() {
        String invalidOwnerId = "short-id";

        webTestClient.delete()
                .uri("/api/v2/gateway/owners/{ownerId}", invalidOwnerId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void whenDeleteOwner_withNonExistentOwner_thenReturnBadRequest() {
        String validLengthOwnerId = "12345678-1234-1234-1234-123456789012";

        webTestClient.delete()
                .uri("/api/v2/gateway/owners/{ownerId}", validLengthOwnerId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }
}