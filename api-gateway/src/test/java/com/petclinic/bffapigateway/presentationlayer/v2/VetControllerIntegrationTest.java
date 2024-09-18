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
        mockServerConfigVetService.registerAddVetEndpoint();

        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerValidateTokenForVetEndpoint();

    }

    @AfterAll
    public void stopMockServer() {
        mockServerConfigVetService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    private static final String VET_ENDPOINT = "/api/v2/gateway/vet";
    private static final String BEARER_TOKEN = jwtTokenForValidAdmin;

    //#region Dummy data
    Set<Workday> workdaySet = Set.of(Workday.Wednesday);

    VetRequestDTO newVetRequestDTO = VetRequestDTO.builder()
            .vetBillId("bill001")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phoneNumber("1234567890")
            .resume("Specialist in dermatology")
            .workday(workdaySet)
            .workHoursJson("08:00-16:00")
            .active(true)
            .specialties(Set.of(SpecialtyDTO.builder().specialtyId("dermatology").name("Dermatology").build()))
            .photoDefault(false)
            .build();
    //#endregion

    @Test
    void whenAddVet_asAdmin_thenReturnCreatedVetResponseDTO() {

        Mono<VetResponseDTO> result = webTestClient.post()
                .uri("/api/v2/gateway/vet")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newVetRequestDTO), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(VetResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(vetResponseDTO -> {
                    assertNotNull(vetResponseDTO);
                    assertNotNull(vetResponseDTO.getVetId());
                    assertEquals(newVetRequestDTO.getVetBillId(), vetResponseDTO.getVetBillId());
                    assertEquals(newVetRequestDTO.getFirstName(), vetResponseDTO.getFirstName());
                    assertEquals(newVetRequestDTO.getLastName(), vetResponseDTO.getLastName());
                    assertEquals(newVetRequestDTO.getEmail(), vetResponseDTO.getEmail());
                    assertEquals(newVetRequestDTO.getPhoneNumber(), vetResponseDTO.getPhoneNumber());
                    assertEquals(newVetRequestDTO.getResume(), vetResponseDTO.getResume());
                    assertEquals(newVetRequestDTO.getWorkday(), vetResponseDTO.getWorkday());
                    assertEquals(newVetRequestDTO.getWorkHoursJson(), vetResponseDTO.getWorkHoursJson());
                    assertEquals(newVetRequestDTO.isActive(), vetResponseDTO.isActive());
                    assertEquals(newVetRequestDTO.getSpecialties(), vetResponseDTO.getSpecialties());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenAddVet_asARoleOtherThanAdmin_thenReturnIsUnauthorized() {

        webTestClient.post()
                .uri("/api/v2/gateway/vet")
                .cookie("Bearer", jwtTokenForInvalidOwnerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newVetRequestDTO), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }




}