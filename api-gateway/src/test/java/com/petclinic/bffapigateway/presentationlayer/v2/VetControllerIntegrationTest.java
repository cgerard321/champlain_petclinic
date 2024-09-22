package com.petclinic.bffapigateway.presentationlayer.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.SpecialtyDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetRequestDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.Workday;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.*;
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
        mockServerConfigVetService.registerGetVetsEndpoint();
        mockServerConfigVetService.registerGetVetsEndpoint_withNoVets();
        mockServerConfigVetService.registerUpdateVetEndpoint();

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
    public void whenGetVets_thenReturnVets() {

        webTestClient.get()
                .uri(VET_ENDPOINT)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(VetResponseDTO.class)
                .hasSize(2);
    }

    @Test
    public void whenGetVets_withNoVets_thenReturnNotFound() {

            webTestClient.get()
                    .uri("/vets")
                    .cookie("Bearer", jwtTokenForValidAdmin)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound();
    }

    @Test
    void whenAddVet_asAdmin_thenReturnCreatedVetResponseDTO() {

        Mono<VetResponseDTO> result = webTestClient.post()
                .uri("/api/v2/gateway/vets")
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
                .uri("/api/v2/gateway/vets")
                .cookie("Bearer", jwtTokenForInvalidOwnerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newVetRequestDTO), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }



    @Test
    void whenGetVetByFirstName_notExists_thenReturnNotFound() {
        String firstName = "Unknown";

        mockServerConfigVetService.registerGetVetByFirstNameEndpointNotFound(firstName);

        webTestClient.get()
                .uri("/api/v2/gateway/vets/firstName/{firstName}", firstName)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    void whenGetVetByLastName_notExists_thenReturnNotFound() {
        String lastName = "Unknown";

        mockServerConfigVetService.registerGetVetByLastNameEndpointNotFound(lastName);

        webTestClient.get()
                .uri("/api/v2/gateway/vets/lastName/{lastName}", lastName)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenUpdateVet_asAdmin_with_ValidVetId_thenReturnUpdatedVetResponseDTO() {

        VetRequestDTO updatedRequestDTO = VetRequestDTO.builder()
                .vetId("c02cbf82-625b-11ee-8c99-0242ac120002")
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



        Mono<VetResponseDTO> result = webTestClient.put()
                .uri("/api/v2/gateway/vets/{vetId}", updatedRequestDTO.getVetId())
                .cookie("Bearer", jwtTokenForValidAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedRequestDTO), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(VetResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(vetResponseDTO -> {
                    assertNotNull(vetResponseDTO);
                    assertEquals(updatedRequestDTO.getVetId(), vetResponseDTO.getVetId());
                    assertEquals(updatedRequestDTO.getVetBillId(), vetResponseDTO.getVetBillId());
                    assertEquals(updatedRequestDTO.getFirstName(), vetResponseDTO.getFirstName());
                    assertEquals(updatedRequestDTO.getLastName(), vetResponseDTO.getLastName());
                    assertEquals(updatedRequestDTO.getEmail(), vetResponseDTO.getEmail());
                    assertEquals(updatedRequestDTO.getPhoneNumber(), vetResponseDTO.getPhoneNumber());
                    assertEquals(updatedRequestDTO.getResume(), vetResponseDTO.getResume());
                    assertEquals(updatedRequestDTO.getWorkday(), vetResponseDTO.getWorkday());
                    assertEquals(updatedRequestDTO.getWorkHoursJson(), vetResponseDTO.getWorkHoursJson());
                    assertEquals(updatedRequestDTO.isActive(), vetResponseDTO.isActive());
                    assertEquals(updatedRequestDTO.getSpecialties(), vetResponseDTO.getSpecialties());
                    return true;
                })
                .verifyComplete();
    }



   /* @Test
    void whenUpdateVet_asAdmin_with_InvalidVetId_thenReturnInvalidInputException() {

        VetRequestDTO updatedRequestDTO = VetRequestDTO.builder()
                .vetId("invalid-vet-id")
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

        Mono<InvalidInputException> result = webTestClient.put()
                .uri("/api/v2/gateway/vets/{vetId}", updatedRequestDTO.getVetId())
                .cookie("Bearer", jwtTokenForInvalidVetId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedRequestDTO), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(InvalidInputException.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(responseError -> {
                    assertNotNull(responseError);
                    assertEquals("Vet Id provided is invalid:" + updatedRequestDTO.getVetId(), responseError.getMessage());
                return true;
                })
                .verifyComplete();
    }
*/






}
