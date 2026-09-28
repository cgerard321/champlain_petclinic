package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerResponseDTO;
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
class CustomerControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigCustomersService mockServerConfigCustomersService;

    private MockServerConfigAuthService mockServerConfigAuthService;

    @BeforeAll
    public void startMockServer() {
        mockServerConfigCustomersService = new MockServerConfigCustomersService();
        mockServerConfigCustomersService.registerUpdateCustomerEndpoint();
        mockServerConfigCustomersService.registerAddCustomerEndpoint();
        mockServerConfigCustomersService.registerGetAllCustomersEndpoint();
        mockServerConfigCustomersService.registerDeleteCustomerEndpoint();
        mockServerConfigCustomersService.registerDeleteCustomerEmptyResponseEndpoint();
        mockServerConfigCustomersService.registerGetCustomerByIdEndpoint();

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
    public void whenDeleteCustomer_asAdmin_thenReturnCustomerResponse() {
        // Mock data to simulate the CustomerResponseDTO
        CustomerResponseDTO expectedCustomer = new CustomerResponseDTO();
        expectedCustomer.setCustomerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
        expectedCustomer.setFirstName("Betty");
        expectedCustomer.setLastName("Davis");
        expectedCustomer.setAddress("638 Cardinal Ave.");
        expectedCustomer.setCity("Sun Prairie");
        expectedCustomer.setProvince("Quebec");
        expectedCustomer.setTelephone("6085551749");

        // Perform the DELETE request and expect CustomerResponseDTO in the body
        webTestClient.delete()
                .uri("/api/v2/gateway/customers/{customerId}", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .cookie("Bearer", "valid-test-token-for-valid-admin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Now we expect a 200 OK response, not 204 NO_CONTENT
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerResponseDTO.class)
                .value(customerResponseDTO -> {
                    assertThat(customerResponseDTO.getCustomerId()).isEqualTo(expectedCustomer.getCustomerId());
                    assertThat(customerResponseDTO.getFirstName()).isEqualTo(expectedCustomer.getFirstName());
                    assertThat(customerResponseDTO.getLastName()).isEqualTo(expectedCustomer.getLastName());
                    assertThat(customerResponseDTO.getAddress()).isEqualTo(expectedCustomer.getAddress());
                    assertThat(customerResponseDTO.getCity()).isEqualTo(expectedCustomer.getCity());
                    assertThat(customerResponseDTO.getProvince()).isEqualTo(expectedCustomer.getProvince());
                    assertThat(customerResponseDTO.getTelephone()).isEqualTo(expectedCustomer.getTelephone());
                });
    }


    @Test
    void whenDeleteCustomer_withInvalidCustomerIdLength_thenReturnUnprocessableEntity() {
        String invalidCustomerId = "short-id";

        webTestClient.delete()
                .uri("/api/v2/gateway/customers/{customerId}", invalidCustomerId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void whenDeleteCustomer_withNonExistentCustomer_thenReturnBadRequest() {
        String validLengthCustomerId = "12345678-1234-1234-1234-123456789012";

        webTestClient.delete()
                .uri("/api/v2/gateway/customers/{customerId}", validLengthCustomerId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }
}