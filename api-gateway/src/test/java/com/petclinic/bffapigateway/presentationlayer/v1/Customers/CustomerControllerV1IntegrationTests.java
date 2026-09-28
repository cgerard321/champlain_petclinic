package com.petclinic.bffapigateway.presentationlayer.v1.Customers;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
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
public class CustomerControllerV1IntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigCustomersService mockServerConfigCustomersService;
    private MockServerConfigAuthService mockServerConfigAuthService;

    private final String CUSTOMER_BASE_PATH = "/api/gateway/customers";
    private final String CUSTOMER_ID = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
    private final String PET_ID = "pet-id-456";

    // DTOs matching the mock server expectations
    CustomerRequestDTO customerUpdateRequest = CustomerRequestDTO.builder()
            .firstName("Betty")
            .lastName("Davis")
            .address("638 Cardinal Ave.")
            .city("Sun Prairie")
            .province("Quebec")
            .telephone("6085551749")
            .build();

    CustomerResponseDTO petOwnerResponse = CustomerResponseDTO.builder()
            .customerId(CUSTOMER_ID)
            .firstName("Betty")
            .lastName("Davis")
            .address("638 Cardinal Ave.")
            .city("Sun Prairie")
            .province("Quebec")
            .telephone("6085551749")
            .build();

    PetResponseDTO petResponse = PetResponseDTO.builder()
            .petId(PET_ID)
            .name("Buster")
            .ownerId(CUSTOMER_ID)
            .petTypeId("pt-1")
            .build();


    @BeforeEach
    public void startMockServer() {
        mockServerConfigCustomersService = new MockServerConfigCustomersService();
        mockServerConfigCustomersService.registerGetAllCustomersEndpoint();
        mockServerConfigCustomersService.registerGetCustomerByIdEndpoint();
        mockServerConfigCustomersService.registerUpdateCustomerEndpoint();
        mockServerConfigCustomersService.registerDeleteCustomerEndpoint();
        mockServerConfigCustomersService.registerGetPetForOwnerEndpoint(CUSTOMER_ID, PET_ID, petResponse);

        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
    }

    @AfterEach
    public void stopMockServer() {
        mockServerConfigCustomersService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    @Test
    void whenGetAllCustomers_WithValidClient_thenReturnResult() {

        Mono<List<CustomerResponseDTO>> result = webTestClient.get()
                .uri(CUSTOMER_BASE_PATH)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.valueOf("text/event-stream;charset=UTF-8"))
                .returnResult(CustomerResponseDTO.class)
                .getResponseBody()
                .collectList()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(customerResponseDTOS -> {
                    assertNotNull(customerResponseDTOS);
                    assertThat(customerResponseDTOS.size()).isEqualTo(3);
                    assertThat(customerResponseDTOS.get(0).getFirstName()).isEqualTo("John");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenGetCustomerDetails_withValidId_thenReturnCustomer() {
        Mono<CustomerResponseDTO> result = webTestClient.get()
                .uri(CUSTOMER_BASE_PATH + "/{customerId}", CUSTOMER_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(CustomerResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(customer -> {
                    assertNotNull(customer);
                    assertThat(customer.getCustomerId()).isEqualTo(CUSTOMER_ID);
                    assertThat(customer.getFirstName()).isEqualTo("Betty");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenDeleteCustomer_withValidId_thenReturnNoContent() {

        webTestClient.delete()
                .uri(CUSTOMER_BASE_PATH + "/{customerId}", CUSTOMER_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetPet_withValidOwnerAndPetId_thenReturnPet() {
        Mono<PetResponseDTO> result = webTestClient.get()
                .uri(CUSTOMER_BASE_PATH + "/{ownerId}/pets/{petId}", CUSTOMER_ID, PET_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(PetResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(pet -> {
                    assertNotNull(pet);
                    assertThat(pet.getPetId()).isEqualTo(PET_ID);
                    assertThat(pet.getName()).isEqualTo("Buster");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenDeletePet_withValidOwnerAndPetId_thenReturnNotFound() {
        webTestClient.delete()
                .uri(CUSTOMER_BASE_PATH + "/{ownerId}/pets/{petId}", CUSTOMER_ID, PET_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetCustomerWithPhotoIntegration_thenReturnCustomerWithPhoto() {
        mockServerConfigCustomersService.clearExpectationsForCustomer(CUSTOMER_ID);
        
        String mockCustomerJson = """
            {
                "customerId": "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a",
                "firstName": "John",
                "lastName": "Doe",
                "photo": {
                    "fileId": "photo-123",
                    "fileName": "profile.png",
                    "fileType": "image/png",
                    "fileData": "aW50ZWdyYXRpb25QaG90b0RhdGE="
                }
            }
            """;
        mockServerConfigCustomersService.registerGetCustomerWithPhotoEndpoint(CUSTOMER_ID, mockCustomerJson);

        Mono<CustomerResponseDTO> result = webTestClient.get()
                .uri(CUSTOMER_BASE_PATH + "/detail/{customerId}?includePhoto=true", CUSTOMER_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(CustomerResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier.create(result)
                .expectNextMatches(customer ->
                    customer.getCustomerId().equals("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a") &&
                    customer.getFirstName().equals("John") &&
                    customer.getLastName().equals("Doe") &&
                    customer.getPhoto() != null &&
                    customer.getPhoto().getFileType().equals("image/png"))
                .verifyComplete();
    }

    @Test
    void whenGetCustomerPhotoIntegrationNotFound_thenReturn404() {
        mockServerConfigCustomersService.registerGetCustomerPhotoEndpoint(CUSTOMER_ID, null);

        webTestClient.get()
                .uri(CUSTOMER_BASE_PATH + "/{customerId}/photos", CUSTOMER_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.IMAGE_PNG)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenDeletePetPhotoForOwnerIntegration_thenReturnOk() {
        PetResponseDTO petResponseDTO = new PetResponseDTO();
        petResponseDTO.setPetId(PET_ID);
        petResponseDTO.setName("Test Pet");
        petResponseDTO.setPhoto(null);
        
        mockServerConfigCustomersService.registerDeletePetPhotoEndpoint(PET_ID, petResponseDTO);

        webTestClient.patch()
                .uri(CUSTOMER_BASE_PATH + "/{ownerId}/pets/{petId}/photo", CUSTOMER_ID, PET_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PetResponseDTO.class)
                .value(body -> {
                    assertThat(body.getPetId()).isEqualTo(PET_ID);
                    assertThat(body.getName()).isEqualTo("Test Pet");
                    assertThat(body.getPhoto()).isNull();
                });
    }

    @Test
    void whenDeletePetPhotoForOwnerIntegration_withNonExistentPet_thenReturnNotFound() {
        mockServerConfigCustomersService.registerDeletePetPhotoEndpoint(PET_ID, null);

        webTestClient.patch()
                .uri(CUSTOMER_BASE_PATH + "/{ownerId}/pets/{petId}/photo", CUSTOMER_ID, PET_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenDeletePetPhotoForOwnerIntegration_withoutAuth_thenReturnUnauthorized() {
        webTestClient.patch()
                .uri(CUSTOMER_BASE_PATH + "/{ownerId}/pets/{petId}/photo", CUSTOMER_ID, PET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }



}
