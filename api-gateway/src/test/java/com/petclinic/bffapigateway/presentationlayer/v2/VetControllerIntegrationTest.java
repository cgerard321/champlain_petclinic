package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.SpecialtyDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetRequestDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.Workday;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigCustomersService;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigVetService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.jwtTokenForInvalidOwnerId;
import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.jwtTokenForValidAdmin;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VetControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigVetService mockServerConfigVetService;

    private MockServerConfigAuthService mockServerConfigAuthService;

    @BeforeAll
    public void startMockServer() {
        mockServerConfigVetService = new MockServerConfigVetService();
        mockServerConfigAuthService = new MockServerConfigAuthService();

        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerValidateTokenForVetEndpoint();
    }

    @AfterAll
    public void stopMockServer() {
        mockServerConfigVetService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    private static final String VET_ENDPOINT = "/api/v2/gateway/vets";
    private static final String BEARER_TOKEN = "Bearer " + jwtTokenForValidAdmin;

    @Test
    public void getVetByVetId_ValidId_ReturnsVet() {

        String validVetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";

        VetResponseDTO expectedVetResponse = VetResponseDTO.builder()
                .vetId(validVetId)
                .vetBillId("5")
                .firstName("Henry")
                .lastName("Stevens")
                .email("stevenshenry@email.com")
                .phoneNumber("(514)-634-8276 #2389")
                .resume("Practicing since 1 years")
                .workday(Set.of(Workday.Wednesday, Workday.Tuesday, Workday.Thursday, Workday.Monday))
                .workHoursJson("{\"Thursday\":[\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\"],"
                        + "\"Monday\":[\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\","
                        + "\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],"
                        + "\"Wednesday\":[\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\","
                        + "\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"],"
                        + "\"Tuesday\":[\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\","
                        + "\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"]}")
                .active(false)
                .specialties(Set.of(
                        SpecialtyDTO.builder()
                                .specialtyId("surgery")
                                .name("surgery")
                                .build(),
                        SpecialtyDTO.builder()
                                .specialtyId("radiology")
                                .name("radiology")
                                .build()))
                .build();


        webTestClient.get()
                .uri(VET_ENDPOINT + "/" + validVetId)
                .header(AUTHORIZATION, BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(VetResponseDTO.class)
                .consumeWith(response -> {
                    VetResponseDTO vetResponse = response.getResponseBody();
                    assertNotNull(vetResponse);
                    assertEquals(expectedVetResponse.getVetId(), vetResponse.getVetId());
                    assertEquals("Henry", vetResponse.getFirstName());
                    assertEquals("Stevens", vetResponse.getLastName());
                    assertEquals("stevenshenry@email.com", vetResponse.getEmail());
                    assertEquals("(514)-634-8276 #2389", vetResponse.getPhoneNumber());
                    assertEquals("Practicing since 1 years", vetResponse.getResume());
                    assertEquals(expectedVetResponse.getWorkday(), vetResponse.getWorkday());
                    assertEquals(expectedVetResponse.getWorkHoursJson(), vetResponse.getWorkHoursJson());
                    assertEquals(expectedVetResponse.isActive(), vetResponse.isActive());
                    assertEquals(expectedVetResponse.getSpecialties().size(), vetResponse.getSpecialties().size());
                });
    }

    @Test
    public void getVetByVetId_InvalidId_ReturnsNotFound() {

        String invalidVetId = "ac9adeb8-625b-11ee-8c99-0242ac12000200000";

        webTestClient.get()
                .uri(VET_ENDPOINT + "/" + invalidVetId)
                .header(AUTHORIZATION, BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()  // Expect 404 NOT FOUND
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertTrue(responseBody.contains("vetId not found: ac9adeb8-625b-11ee-8c99-0242ac12000200000"));
                });
    }

}
