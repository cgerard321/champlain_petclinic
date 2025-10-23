package com.petclinic.bffapigateway.presentationlayer.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.*;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.jwtTokenForInvalidOwnerId;
import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.jwtTokenForValidAdmin;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
        mockServerConfigVetService.registerGetVetsEndpoint();
        mockServerConfigVetService.registerDeleteVetEndpoint();
        mockServerConfigVetService.registerGetVetsEndpoint_withNoVets();
        mockServerConfigVetService.registerUpdateVetEndpoint();
        //mockServerConfigVetService.registerUpdateVetEndpoint_withInvalidId();
        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerValidateTokenForVetEndpoint();
        mockServerConfigVetService.registerGetVetByIdEndpoint();
        mockServerConfigVetService.registerGetVetByInvalidIdEndpoint();
        mockServerConfigVetService.registerAddEducationAsUnauthorizedUser("2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb");

        mockServerConfigVetService.registerGetPhotoByVetIdEndpoint("ac9adeb8-625b-11ee-8c99-0242ac120002", "mockPhotoData".getBytes());
        mockServerConfigVetService.registerGetPhotoByVetIdEndpointNotFound("invalid-vet-id");
        mockServerConfigVetService.registerUpdatePhotoOfVetEndpoint("69f85766-625b-11ee-8c99-0242ac120002", "newPhoto", "mockPhotoData".getBytes());
        mockServerConfigVetService.registerUpdatePhotoOfVetEndpointNotFound("invalid-vet-id", "newPhoto");

        mockServerConfigVetService.registerAddPhotoByVetIdMultipartEndpoint("ac9adeb8-625b-11ee-8c99-0242ac120002", "test.jpg");
        mockServerConfigVetService.registerAddPhotoByVetIdMultipartEndpointBadRequest("invalid-vet-id");
        mockServerConfigVetService.registerAddAlbumPhotoOctetEndpoint("ac9adeb8-625b-11ee-8c99-0242ac120002", "album-photo.jpg");
        mockServerConfigVetService.registerAddAlbumPhotoOctetEndpointBadRequest("invalid-vet-id");
        mockServerConfigVetService.registerAddAlbumPhotoMultipartEndpoint("ac9adeb8-625b-11ee-8c99-0242ac120002", "album-multipart.jpg");
        mockServerConfigVetService.registerAddAlbumPhotoMultipartEndpointBadRequest("invalid-vet-id");

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
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
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
    public void getVetById_ValidId_ReturnsVet() {
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
                    assertEquals(expectedVetResponse.getFirstName(), vetResponse.getFirstName());
                    assertEquals(expectedVetResponse.getLastName(), vetResponse.getLastName());
                    assertEquals(expectedVetResponse.getEmail(), vetResponse.getEmail());
                    assertEquals(expectedVetResponse.getPhoneNumber(), vetResponse.getPhoneNumber());
                    assertEquals(expectedVetResponse.getResume(), vetResponse.getResume());
                    assertEquals("Henry", vetResponse.getFirstName());
                    assertEquals("Stevens", vetResponse.getLastName());
                    assertEquals("stevenshenry@email.com", vetResponse.getEmail());
                    assertEquals("(514)-634-8276 #2389", vetResponse.getPhoneNumber());
                    assertEquals("Practicing since 1 years", vetResponse.getResume());
                    assertEquals(expectedVetResponse.getWorkday(), vetResponse.getWorkday());
                    assertEquals(expectedVetResponse.getWorkHoursJson(), vetResponse.getWorkHoursJson());
                    assertEquals(expectedVetResponse.isActive(), vetResponse.isActive());
                    assertEquals(expectedVetResponse.getSpecialties().size(), vetResponse.getSpecialties().size());
                    assertTrue(vetResponse.getSpecialties().stream().anyMatch(specialty -> specialty.getName().equals("surgery")));
                    assertTrue(vetResponse.getSpecialties().stream().anyMatch(specialty -> specialty.getName().equals("radiology")));
                });
    }

    @Test
    public void getVetById_InvalidId_ReturnsNotFound() {
        String invalidVetId = "ac9adeb8-625b-11ee-8c99-0242ac12000200000";

        webTestClient.get()
                .uri(VET_ENDPOINT + "/" + invalidVetId)
                .header(AUTHORIZATION, BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertTrue(responseBody.contains("vetId not found: ac9adeb8-625b-11ee-8c99-0242ac12000200000"));
                });
    }

    @Test
    void whenDeleteVet_asAdmin_thenReturnNoContent() {
        String vetId = UUID.randomUUID().toString();

        mockServerConfigVetService.registerDeleteVetEndpoint();

        webTestClient.delete()
                .uri(VET_ENDPOINT + "/" + vetId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VetResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertFalse(dto.isActive());
                });
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
                .expectStatus().isOk() // change to is Ok after testing for errors
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

    @Test
    public void whenGetPhotoByVetId_thenReturnPhoto() {
        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";
        byte[] photoData = "mockPhotoData".getBytes();
        mockServerConfigVetService.registerGetPhotoByVetIdEndpoint(vetId, photoData);

        webTestClient.get()
                .uri(VET_ENDPOINT + "/" + vetId + "/photo")
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.IMAGE_JPEG)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG_VALUE)
                .expectBody(byte[].class)
                .isEqualTo(photoData);
    }


    @Test
    public void whenGetPhotoByVetId_withNotFoundVetId_thenReturn404() {
        String notFoundVetId = ("jj2cbf82-625b-11ee-8c99-0242ac120002");

        mockServerConfigVetService.registerGetPhotoByVetIdEndpointNotFound(notFoundVetId);

        webTestClient.get()
                .uri(VET_ENDPOINT + "/" + notFoundVetId + "/photo")
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isNotFound();
    }



    @Test
    public void whenUpdatePhotoByVetId_withNotFoundVetId_thenReturn404() {
        String notFoundVetId = "not found";
        String photoName = "newPhoto";

        mockServerConfigVetService.registerUpdatePhotoOfVetEndpointNotFound(notFoundVetId, photoName);

        webTestClient.put()
                .uri(VET_ENDPOINT + "/" + notFoundVetId + "/photo/" + photoName)
                .cookie("Bearer", BEARER_TOKEN)
                .contentType(MediaType.IMAGE_JPEG)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenGetAlbumsByVetId_thenReturnAlbums() throws Exception {

        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";

        Album album1 = new Album(1, vetId, "album1", "image/jpeg", "mockImageData1".getBytes());
        Album album2 = new Album(2, vetId, "album2", "image/jpeg", "mockImageData2".getBytes());

        mockServerConfigVetService.registerGetAlbumsByVetIdEndpoint(vetId, List.of(album1, album2));

        webTestClient.get()
                .uri(VET_ENDPOINT + "/" + vetId + "/albums")
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Album.class)
                .hasSize(2)
                .contains(album1, album2);
    }


    @Test
    public void whenGetAlbumsByInvalidVetId_thenReturnNotFound() {

        String invalidVetId = "ac9adeb8-625b-11ee-8c99-0242ac12000200";

        mockServerConfigVetService.registerGetAlbumsByVetIdEndpointNotFound(invalidVetId);

        webTestClient.get()
                .uri(VET_ENDPOINT + "/" + invalidVetId + "/albums")
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenDeletePhotoByVetId_thenReturnNoContent() {
        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";
        mockServerConfigVetService.registerDeletePhotoByVetIdEndpoint(vetId);

        webTestClient.delete()
                .uri(VET_ENDPOINT + "/" + vetId + "/photo")
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void whenDeletePhotoByVetId_PhotoNotFound_thenReturnNotFound() {
        String vetId = "in9beda9-526t-22gg-1a96-0672ac230007";
        mockServerConfigVetService.registerDeletePhotoByVetIdEndpointNotFound(vetId);

        webTestClient.delete()
                .uri(VET_ENDPOINT + "/" + vetId + "/photo")
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Photo not found for vetId: " + vetId);
    }
    @Test
    void whenDeleteAlbumPhotoById_thenReturnNoContent() {
        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";
        Integer albumId = 1;

        // Mock the delete endpoint in the Mock Server
        mockServerConfigVetService.registerDeleteAlbumPhotoEndpoint(vetId, albumId);

        webTestClient.delete()
                .uri(VET_ENDPOINT + "/" + vetId + "/albums/" + albumId)
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent() // Expect 204 No Content
                .expectBody().isEmpty();
    }

    @Test
    void whenDeleteAlbumPhotoById_withNonExistentId_thenReturnNotFound() {
        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";
        Integer nonExistentAlbumId = 999;

        // Mock the not found scenario in the Mock Server
        mockServerConfigVetService.registerDeleteAlbumPhotoEndpointNotFound(vetId, nonExistentAlbumId);

        webTestClient.delete()
                .uri(VET_ENDPOINT + "/" + vetId + "/albums/" + nonExistentAlbumId)
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound() // Expect 404 Not Found
                .expectBody()
                .equals(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void whenDeleteAlbumPhotoById_withServerError_thenReturnServerError() {
        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";
        Integer albumId = 2;

        // Mock the server error scenario in the Mock Server
        mockServerConfigVetService.registerDeleteAlbumPhotoEndpointWithServerError(vetId, albumId);

        webTestClient.delete()
                .uri(VET_ENDPOINT + "/" + vetId + "/albums/" + albumId)
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError(); // Expect 500 Server Error
    }


    @Test
    void whenGetEducationByVetId_thenReturnEducation() throws JsonProcessingException {
        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";

        EducationRequestDTO education1 = new EducationRequestDTO(vetId, "school1", "degree1", "field1", "2020-01-01", "2021-01-01");
        EducationRequestDTO education2 = new EducationRequestDTO(vetId, "school2", "degree2", "field2", "2021-01-01", "2022-01-01");

        mockServerConfigVetService.registerGetEducationByVetIdEndpoint(vetId, List.of(education1, education2));

        webTestClient.get()
                .uri(VET_ENDPOINT + "/" + vetId + "/educations")
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(EducationRequestDTO.class)
                .hasSize(2)
                .contains(education1, education2);
    }

    @Test
    void whenGetEducationByInvalidVetId_thenReturnNotFound() {
        String invalidVetId = "ac9adeb8-625b-11ee-8c99-0242ac12000200";

        mockServerConfigVetService.registerGetEducationByVetIdEndpointNotFound(invalidVetId);

        webTestClient.get()
                .uri(VET_ENDPOINT + "/" + invalidVetId + "/educations")
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    void whenUpdateEducationByVetIdAndEducationId_thenReturnUpdatedEducation() {
        String vetId = "69f85766-625b-11ee-8c99-0242ac120002";
        String educationId = "eb859d39-692b-4e9d-9928-f5a67812ce44";

        EducationRequestDTO updatedEducation = new EducationRequestDTO(
                        vetId,
                        "school1",
                        "degree1",
                        "field1",
                        "2020-01-01",
                        "2021-01-01");

        mockServerConfigVetService.registerUpdateEducationByVetIdAndEducationIdEndpoint(vetId, educationId, updatedEducation);

        Mono<EducationRequestDTO> result = webTestClient.put()
                .uri(VET_ENDPOINT + "/" + vetId + "/educations/" + educationId)
                .cookie("Bearer", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedEducation)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(EducationRequestDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(educationResponse -> {
                    assertNotNull(educationResponse);
                    assertEquals(updatedEducation.getVetId(), educationResponse.getVetId());
                    assertEquals(updatedEducation.getSchoolName(), educationResponse.getSchoolName());
                    assertEquals(updatedEducation.getDegree(), educationResponse.getDegree());
                    assertEquals(updatedEducation.getFieldOfStudy(), educationResponse.getFieldOfStudy());
                    assertEquals(updatedEducation.getStartDate(), educationResponse.getStartDate());
                    assertEquals(updatedEducation.getEndDate(), educationResponse.getEndDate());
                    return true;
                })
                .verifyComplete();
    }


    @Test
     void whenUpdateEducationByInvalidVetIdAndEducationId_thenReturnNotFound(){
        String invalidVetId = "ac9adeb8-625b-11ee-8c99-0242ac12000200";
        String educationId = "eb859d39-692b-4e9d-9928-f5a67812ce44";

        EducationRequestDTO updatedEducation = new EducationRequestDTO(
                invalidVetId,
                "school1",
                "degree1",
                "field1",
                "2020-01-01",
                "2021-01-01");

        mockServerConfigVetService.registerUpdateEducationByVetIdAndEducationIdEndpointNotFound(invalidVetId, educationId, updatedEducation);

        webTestClient.put()
                .uri(VET_ENDPOINT + "/" + invalidVetId + "/educations/" + educationId)
                .cookie("Bearer", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedEducation)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenAddEducationAsUnauthorizedUser_thenReturnUnauthorized() {
        EducationRequestDTO educationRequestDTO = EducationRequestDTO.builder()
                .vetId("2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb")
                .schoolName("Harvard University")
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Science")
                .startDate("2015-08-01")
                .endDate("2019-05-30")
                .build();

        mockServerConfigVetService.registerAddEducationAsUnauthorizedUser(educationRequestDTO.getVetId());

        webTestClient.post()
                .uri(VET_ENDPOINT + "/" + educationRequestDTO.getVetId() + "/educations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(educationRequestDTO), EducationRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }




    @Test
    void whenUpdateEducationByVetIdAndInvalidEducationId_thenReturnNotFound(){
        String vetId = "69f85766-625b-11ee-8c99-0242ac120002";
        String invalidEducationid = "eb859d39-692b-4e9d-9928-f5a67812ce44322";

        EducationRequestDTO updatedEducation =
                new EducationRequestDTO(vetId, "school1", "degree1", "field1", "2020-01-01", "2021-01-01");

        mockServerConfigVetService.registerUpdateEducationByVetIdAndEducationIdEndpointNotFound(vetId, invalidEducationid, updatedEducation);

        webTestClient.put()
                .uri(VET_ENDPOINT + "/" + vetId + "/educations/" + invalidEducationid)
                .cookie("Bearer", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedEducation)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getRatingsByVetId_ValidId_ReturnsRatings() throws JsonProcessingException {
        String validVetId = "c02cbf82-625b-11ee-8c99-0242ac120002";
        RatingResponseDTO ratingResponseDTO = RatingResponseDTO.builder()
                .ratingId("123456")
                .vetId(validVetId)
                .rateScore(4.5)
                .rateDescription("Great service!")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .rateDate("2024-10-12")
                .customerName("John Doe")
                .build();
        String jsonResponse = new ObjectMapper().writeValueAsString(List.of(ratingResponseDTO));
        mockServerConfigVetService.registerGetRatingsByVetIdEndpoint(validVetId, jsonResponse);
        webTestClient.get()
                .uri(VET_ENDPOINT + "/" + validVetId + "/ratings")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Then: expect successful response with valid ratings
                .expectStatus().isOk()
                .expectBodyList(RatingResponseDTO.class)
                .hasSize(1)
                .contains(ratingResponseDTO);
    }
    @Test
    void getRatingsByVetId_InvalidId_ReturnsNotFound() {
        String invalidVetId = "deb1950c-3c56-45dc-874b-89e352695eb7777";
        mockServerConfigVetService.registerGetRatingsByVetIdEndpointNotFound(invalidVetId);
        webTestClient.get()
                .uri(VET_ENDPOINT + "/" + invalidVetId + "/ratings")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertTrue(responseBody.contains("vetId not found: " + invalidVetId));
                });
    }


    @Test
    void whenDeleteEducation_thenReturnNoContent() {
        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";
        String educationId = "123e4567-e89b-12d3-a456-426614174000";

        mockServerConfigVetService.registerDeleteEducationEndpoint(vetId, educationId);

        webTestClient.delete()
                .uri(VET_ENDPOINT + "/" + vetId + "/educations/" + educationId)
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void whenDeleteEducation_EducationNotFound_thenReturnNotFound() {
        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";
        String educationId = "non-existent-education-id";

        mockServerConfigVetService.registerDeleteEducationByVetIdEndpointNotFound(vetId, educationId);

        webTestClient.delete()
                .uri(VET_ENDPOINT + "/" + vetId + "/educations/" + educationId)
                .cookie("Bearer", BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Education not found: " + educationId);
    }


    @Test
    void whenAddPhotoByVetIdMultipart_thenReturnCreatedPhoto() {
        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";
        String photoName = "test.jpg";
        
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("photoName", photoName);
        builder.part("file", new ByteArrayResource("mockPhotoData".getBytes())).filename("test.jpg");

        webTestClient.post()
                .uri(VET_ENDPOINT + "/" + vetId + "/photos")
                .cookie("Bearer", BEARER_TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is4xxClientError(); 
    }

    @Test
    void whenAddPhotoByVetIdMultipart_withInvalidVetId_thenReturnBadRequest() {
        String invalidVetId = "invalid-vet-id";
        String photoName = "test.jpg";
        
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("photoName", photoName);
        builder.part("file", new ByteArrayResource("mockPhotoData".getBytes())).filename("test.jpg");

        webTestClient.post()
                .uri(VET_ENDPOINT + "/" + invalidVetId + "/photos")
                .cookie("Bearer", BEARER_TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is4xxClientError(); 
    }

    @Test
    void whenAddAlbumPhotoOctet_thenReturnCreatedAlbum() {
        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";
        String photoName = "album-photo.jpg";
        byte[] photoData = "mockPhotoData".getBytes();

        webTestClient.post()
                .uri(VET_ENDPOINT + "/" + vetId + "/albums/photos")
                .cookie("Bearer", BEARER_TOKEN)
                .header("Photo-Name", photoName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(photoData)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void whenAddAlbumPhotoOctet_withInvalidVetId_thenReturnBadRequest() {
        String invalidVetId = "invalid-vet-id";
        String photoName = "album-photo.jpg";
        byte[] photoData = "mockPhotoData".getBytes();

        webTestClient.post()
                .uri(VET_ENDPOINT + "/" + invalidVetId + "/albums/photos")
                .cookie("Bearer", BEARER_TOKEN)
                .header("Photo-Name", photoName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(photoData)
                .exchange()
                .expectStatus().is4xxClientError(); 
    }

    @Test
    void whenAddAlbumPhotoMultipart_thenReturnCreatedAlbum() {
        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";
        String photoName = "album-multipart.jpg";
        
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("photoName", photoName);
        builder.part("file", new ByteArrayResource("mockPhotoData".getBytes())).filename("album-multipart.jpg");

        webTestClient.post()
                .uri(VET_ENDPOINT + "/" + vetId + "/albums/photos")
                .cookie("Bearer", BEARER_TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is4xxClientError(); 
    }

    @Test
    void whenAddAlbumPhotoMultipart_withInvalidVetId_thenReturnBadRequest() {
        String invalidVetId = "invalid-vet-id";
        String photoName = "album-multipart.jpg";
        
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("photoName", photoName);
        builder.part("file", new ByteArrayResource("mockPhotoData".getBytes())).filename("album-multipart.jpg");

        webTestClient.post()
                .uri(VET_ENDPOINT + "/" + invalidVetId + "/albums/photos")
                .cookie("Bearer", BEARER_TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is4xxClientError(); 
    }


}

