package com.petclinic.visits.visitsservicenew.PresentationLayer;


import com.petclinic.visits.visitsservicenew.BusinessLayer.Review.ReviewService;
import com.petclinic.visits.visitsservicenew.BusinessLayer.VisitService;
import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.SpecialtyDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Workday;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewResponseDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@WebFluxTest(VisitController.class)
class VisitControllerUnitTest {
    @MockBean
    private VisitService visitService;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private WebTestClient webTestClient;


//    @MockBean
//    private VetsClient vetsClient;
//
//    @MockBean
//    private PetsClient petsClient;

    //    String uuidVisit1 = UUID.randomUUID().toString();
    String uuidVet = UUID.randomUUID().toString();
//    String uuidPet = UUID.randomUUID().toString();
//    String uuidPhoto = UUID.randomUUID().toString();
//    String uuidOwner = UUID.randomUUID().toString();

//    private final String STATUS = "COMPLETED";



    Set<SpecialtyDTO> set = new HashSet<>();
    Set<Workday> workdaySet = new HashSet<>();

    VetDTO vet = VetDTO.builder()
            .vetId(uuidVet)
            .vetBillId("1")
            .firstName("James")
            .lastName("Carter")
            .email("carter.james@email.com")
            .phoneNumber("(514)-634-8276 #2384")
            .imageId("1")
            .resume("Practicing since 3 years")
            .workday(workdaySet)
            .active(true)
            .specialties(set)
            .build();
/*
    Date currentDate =new Date();
    PetResponseDTO petResponseDTO = PetResponseDTO.builder()
            .petTypeId(uuidPet)
            .name("Billy")
            .birthDate(currentDate)
            .photoId(uuidPhoto)
            .ownerId(uuidOwner)
            .build();
    Visit visit1 = buildVisit(uuidVisit1,"this is a dummy description",vet.getVetId());
 */

    private final VisitResponseDTO visitResponseDTO = buildVisitResponseDto();
    private final VisitRequestDTO visitRequestDTO = buildVisitRequestDTO(vet.getVetId());
    private final String Visit_UUID_OK = visitResponseDTO.getVisitId();
    private final String Practitioner_Id_OK = visitResponseDTO.getPractitionerId();
    private final String Pet_Id_OK = visitResponseDTO.getPetId();



    //private final LocalDateTime visitDate = visitResponseDTO.getVisitDate().withSecond(0);
    @Test
    void getAllVisits(){
        Visit visit1 = new Visit(); // replace with your actual Visit object


        when(visitService.getAllVisits(visit1.getDescription())).thenReturn(Flux.just(visitResponseDTO, visitResponseDTO));

        webTestClient.get()
                .uri("/visits")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM + ";charset=UTF-8")
                .returnResult(VisitResponseDTO.class);

        verify(visitService, times(1)).getAllVisits(visit1.getDescription());
    }

    @Test
    void getVisitByVisitId() {
        when(visitService.getVisitByVisitId(anyString(), includePrescription)).thenReturn(Mono.just(visitResponseDTO));

        webTestClient.get()
                .uri("/visits/" + Visit_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visitResponseDTO.getVisitId())
                .jsonPath("$.visitDate").isEqualTo("2024-11-25T13:45:00")
                .jsonPath("$.description").isEqualTo(visitResponseDTO.getDescription())
                .jsonPath("$.petId").isEqualTo(visitResponseDTO.getPetId())
                .jsonPath("$.practitionerId").isEqualTo(visitResponseDTO.getPractitionerId())
                .jsonPath("$.status").isEqualTo("UPCOMING");

        verify(visitService, times(1)).getVisitByVisitId(Visit_UUID_OK, includePrescription);
    }

    @Test
    void getVisitByPractitionerId() {
        when(visitService.getVisitsForPractitioner(anyString())).thenReturn(Flux.just(visitResponseDTO));

        webTestClient.get()
                .uri("/visits/practitioner/{practitionerId}", Practitioner_Id_OK)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .returnResult(VisitResponseDTO.class);

        Mockito.verify(visitService, times(1)).getVisitsForPractitioner(Practitioner_Id_OK);
    }


    @Test
    void getVisitsByPetId() {
        when(visitService.getVisitsForPet(anyString())).thenReturn(Flux.just(visitResponseDTO));

        webTestClient.get()
                .uri("/visits/pets/" + Pet_Id_OK)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM + ";charset=UTF-8")
                .returnResult(VisitResponseDTO.class);

        verify(visitService, times(1)).getVisitsForPet(Pet_Id_OK);
    }

    /*
    @Test
    void getByPractitionerIdAndMonth(){
        when(visitService.getVisitsByPractitionerIdAndMonth(anyInt(), anyInt())).thenReturn(Flux.just(visitResponseDTO));

        webFluxTest.get()
                .uri("/visits/practitioner/" + Practitioner_Id_OK+ "/" + Get_Month)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM + ";charset=UTF-8")
                .returnResult(VisitResponseDTO.class);

        Mockito.verify(visitService, times(1)).getVisitsByPractitionerIdAndMonth(Practitioner_Id_OK, Get_Month);
    }
     */

    @Test
    void addVisit() {
        when(visitService.addVisit(any(Mono.class))).thenReturn(Mono.just(visitResponseDTO));

        webTestClient
                .post()
                .uri("/visits")
                .body(Mono.just(visitRequestDTO), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        verify(visitService, times(1)).addVisit(any(Mono.class));
    }

    @Test
    void updateVisitByVisitId() {


        Mono<VisitRequestDTO> monoVisit = Mono.just(visitRequestDTO);


        webTestClient.put()
                .uri("/visits/" + Visit_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .body(monoVisit, VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        verify(visitService, times(1)).updateVisit(anyString(), any(Mono.class));
    }

    /*
    @Test
    void deleteVisit(){
        webFluxTest.delete()
                .uri("/visits/" + Visit_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(visitService, times(1)).deleteVisit(Visit_UUID_OK);
    }
     */
    @Test
    void deleteVisit_visitId_shouldSucceed() {
        // Arrange
        String visitId = UUID.randomUUID().toString();
        when(visitService.deleteVisit(visitId)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient
                .delete()
                .uri("/visits/{visitId}", visitId)
                .exchange()
                .expectStatus().isNoContent();  // Expecting 204 NO CONTENT status.

        verify(visitService, times(1)).deleteVisit(visitId);
    }

    @Test
    void deleteVisit_NonExistentVisitId_ShouldReturnNotFound() {
        // Arrange
        String invalidVisitId = "fakeId";
        when(visitService.deleteVisit(invalidVisitId)).thenReturn(Mono.error(new NotFoundException("No visit was found with visitId: " + invalidVisitId)));

        // Act & Assert
        webTestClient
                .delete()
                .uri("/visits/{visitId}", invalidVisitId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message", "No visit was found with visitId: " + invalidVisitId);
    }

    @Test
    void deleteAllCancelledVisits_shouldSucceed() {
        // Arrange
        Mockito.when(visitService.deleteAllCancelledVisits()).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient
                .delete()
                .uri("/visits/cancelled")
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(visitService, times(1)).deleteAllCancelledVisits();
    }

    @Test
    void deleteAllCancelledVisits_shouldThrowRuntimeException() throws RuntimeException {
        // Arrange
        Mockito.when(visitService.deleteAllCancelledVisits())
                .thenReturn(Mono.error(new RuntimeException("Failed to delete cancelled visits")));

        // Act & Assert
        webTestClient
                .delete()
                .uri("/visits/cancelled")
                .exchange()
                .expectStatus().is5xxServerError();

        Mockito.verify(visitService, times(1)).deleteAllCancelledVisits();
    }

/*
    private Visit buildVisit(String uuid,String description, String vetId){
        return Visit.builder()
                .visitId(uuid)
                .visitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description(description)
                .petId("2")
                .practitionerId(vetId)
                .status(Status.UPCOMING)
                .build();
    }
*/

    private VisitResponseDTO buildVisitResponseDto() {
        return VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .practitionerId(UUID.randomUUID().toString())
                .status(Status.UPCOMING)
                .build();
    }

    private VisitRequestDTO buildVisitRequestDTO(String vetId) {
        return VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .practitionerId(vetId)
                .status(Status.UPCOMING)
                .build();
    }


    @Test
    public void whenGetAllReview_returnReviewResponseDTO() {
        ReviewResponseDTO reviewResponseDTO1 = ReviewResponseDTO.builder()
                .reviewId(UUID.randomUUID().toString())
                .rating(5)
                .reviewerName("Zako")
                .review("Very good")
                .dateSubmitted(LocalDateTime.now())
                .build();

        ReviewResponseDTO reviewResponseDTO2 = ReviewResponseDTO.builder()
                .reviewId(UUID.randomUUID().toString())
                .rating(2)
                .reviewerName("Zako2")
                .review("Very bad")
                .dateSubmitted(LocalDateTime.now())
                .build();

        when(reviewService.GetAllReviews()).thenReturn(Flux.just(reviewResponseDTO1, reviewResponseDTO2));


        // Act & Assert
        webTestClient
                .get()
                .uri("/visits/reviews")
                .accept(MediaType.TEXT_EVENT_STREAM)  // Use TEXT_EVENT_STREAM instead of JSON
                .exchange()
                .expectStatus().isOk()  // Expect 200 OK
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)  // Expect TEXT_EVENT_STREAM
                .expectBodyList(ReviewResponseDTO.class)  // Expect a list of CourseResponseModel
                .hasSize(2)
                .contains(reviewResponseDTO1, reviewResponseDTO2);

        verify(reviewService, times(1)).GetAllReviews();

    }


    @Test
    public void whenGetReviewById_returnReviewResponseDTO() {

        String reviewId = UUID.randomUUID().toString();
        ReviewResponseDTO reviewResponseDTO1 = ReviewResponseDTO.builder()
                .reviewId(reviewId)
                .rating(5)
                .reviewerName("Zako")
                .review("Very good")
                .dateSubmitted(LocalDateTime.now())
                .build();

        ReviewResponseDTO reviewResponseDTO2 = ReviewResponseDTO.builder()
                .reviewId(UUID.randomUUID().toString())
                .rating(2)
                .reviewerName("Zako2")
                .review("Very bad")
                .dateSubmitted(LocalDateTime.now())
                .build();

        when(reviewService.GetReviewByReviewId(reviewId)).thenReturn(Mono.just(reviewResponseDTO1));


        // Act & Assert
        webTestClient
                .get()
                .uri("/visits/reviews/" + reviewId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Expect 200 OK
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(ReviewResponseDTO.class)
                .isEqualTo(reviewResponseDTO1);

        verify(reviewService, times(1)).GetReviewByReviewId(reviewId);

    }

    @Test
    public void whenAddReview_returnReviewResponseDTO() {
        String reviewId = UUID.randomUUID().toString();
        ReviewResponseDTO reviewResponseDTO1 = ReviewResponseDTO.builder()
                .reviewId(reviewId)
                .rating(5)
                .reviewerName("Zako")
                .review("Very good")
                .dateSubmitted(LocalDateTime.now())
                .build();

        ReviewRequestDTO reviewRequestDTO = ReviewRequestDTO.builder()
                .rating(2)
                .reviewerName("Zako2")
                .review("Very bad")
                .dateSubmitted(LocalDateTime.now())
                .build();

        when(reviewService.AddReview(any(Mono.class))).thenReturn(Mono.just(reviewResponseDTO1));
        webTestClient
                .post()
                .uri("/visits/reviews")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(reviewRequestDTO), ReviewRequestDTO.class)
                .exchange()
                .expectStatus().isCreated()  // Expect 200 OK
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ReviewResponseDTO.class)
                .isEqualTo(reviewResponseDTO1);

        verify(reviewService, times(1)).AddReview(any(Mono.class));


    }


    @Test
    public void whenUpdateReview_returnReviewResponseDTO() {
        String reviewId = UUID.randomUUID().toString();
        ReviewResponseDTO reviewResponseDTO1 = ReviewResponseDTO.builder()
                .reviewId(reviewId)
                .rating(5)
                .reviewerName("Zako")
                .review("Very good")
                .dateSubmitted(LocalDateTime.now())
                .build();

        ReviewRequestDTO reviewRequestDTO = ReviewRequestDTO.builder()
                .rating(2)
                .reviewerName("Zako2")
                .review("Very bad")
                .dateSubmitted(LocalDateTime.now())
                .build();

        when(reviewService.UpdateReview(any(Mono.class), anyString())).thenReturn(Mono.just(reviewResponseDTO1));
        webTestClient
                .put()
                .uri("/visits/reviews/" + reviewId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(reviewRequestDTO), ReviewRequestDTO.class)
                .exchange()
                .expectStatus().isOk()  // Expect 200 OK
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ReviewResponseDTO.class)
                .isEqualTo(reviewResponseDTO1);

        verify(reviewService, times(1)).UpdateReview(any(Mono.class), anyString());


    }


    @Test
    public void whenDeleteReviewById_returnReviewResponseDTO() {

        String reviewId = UUID.randomUUID().toString();
        ReviewResponseDTO reviewResponseDTO1 = ReviewResponseDTO.builder()
                .reviewId(reviewId)
                .rating(5)
                .reviewerName("Zako")
                .review("Very good")
                .dateSubmitted(LocalDateTime.now())
                .build();

        ReviewResponseDTO reviewResponseDTO2 = ReviewResponseDTO.builder()
                .reviewId(UUID.randomUUID().toString())
                .rating(2)
                .reviewerName("Zako2")
                .review("Very bad")
                .dateSubmitted(LocalDateTime.now())
                .build();

        when(reviewService.DeleteReview(reviewId)).thenReturn(Mono.just(reviewResponseDTO1));


        // Act & Assert
        webTestClient
                .delete()
                .uri("/visits/reviews/" + reviewId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Expect 200 OK
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(ReviewResponseDTO.class)
                .isEqualTo(reviewResponseDTO1);

        verify(reviewService, times(1)).DeleteReview(reviewId);

    }

    @Test
    void updateVisitStatus_ShouldReturnOK_WhenStatusUpdatedToCancelled() {
        String visitId = "12345";
        String status = "CANCELLED";

        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId(visitId)
                .status(Status.CANCELLED)
                .description("Test visit with cancelled status")
                .build();

        // Mocking the service layer to return the expected response
        when(visitService.patchVisitStatusInVisit(eq(visitId), eq(status)))
                .thenReturn(Mono.just(visitResponseDTO));

        webTestClient.patch()
                .uri("/visits/{visitId}/{status}", visitId, status)
                .exchange()
                .expectStatus().isOk() // Expect 200 OK
                .expectBody(VisitResponseDTO.class)
                .value(response -> {
                    assertEquals(response.getVisitId(), visitId);
                    assertEquals(response.getStatus(), Status.CANCELLED);
                });

        // Verify that the service was called with the correct parameters
        verify(visitService, times(1)).patchVisitStatusInVisit(eq(visitId), eq(status));
    }

    @Test
    void updateVisitStatus_ShouldReturnNotFound_WhenVisitDoesNotExist() {
        String visitId = "nonExistentVisitId";
        String status = "CANCELLED";

        // Mocking the service to return an empty Mono, simulating a not found scenario
        when(visitService.patchVisitStatusInVisit(eq(visitId), eq(status)))
                .thenReturn(Mono.empty());

        webTestClient.patch()
                .uri("/visits/{visitId}/{status}", visitId, status)
                .exchange()
                .expectStatus().isNotFound(); // Expect 404 NOT_FOUND

        // Verify that the service was called
        verify(visitService, times(1)).patchVisitStatusInVisit(eq(visitId), eq(status));
    }


    @Test
    void whenGetAllArchivedVisits_returnVisitResponseDTO() {
        VisitResponseDTO visitResponseDTO1 = VisitResponseDTO.builder()
                .visitId(UUID.randomUUID().toString())
                .visitDate(LocalDateTime.of(2024, 10, 5, 1, 56)) // No seconds or nanoseconds
                .description("Visit 1")
                .petId(UUID.randomUUID().toString())
                .practitionerId(UUID.randomUUID().toString())
                .status(Status.ARCHIVED)
                .build();

        VisitResponseDTO visitResponseDTO2 = VisitResponseDTO.builder()
                .visitId(UUID.randomUUID().toString())
                .visitDate(LocalDateTime.of(2024, 10, 5, 1, 56)) // No seconds or nanoseconds
                .description("Visit 2")
                .petId(UUID.randomUUID().toString())
                .practitionerId(UUID.randomUUID().toString())
                .status(Status.ARCHIVED)
                .build();

        when(visitService.getAllArchivedVisits()).thenReturn(Flux.just(visitResponseDTO1, visitResponseDTO2));

        webTestClient
                .get()
                .uri("/visits/archived")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(VisitResponseDTO.class)
                .hasSize(2)
                .contains(visitResponseDTO1, visitResponseDTO2);

        verify(visitService, times(1)).getAllArchivedVisits();
    }

    @Test
    void whenCompletedVisitWithValidVisitId_ArchiveVisit() {
        String visitId = UUID.randomUUID().toString();
        VisitRequestDTO visitRequestDTO = buildVisitRequestDTO(UUID.randomUUID().toString());
        VisitResponseDTO visitResponseDTO = buildVisitResponseDto();

        when(visitService.archiveCompletedVisit(anyString(), any(Mono.class))).thenReturn(Mono.just(visitResponseDTO));

        webTestClient
                .put()
                .uri("/visits/completed/" + visitId + "/archive")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(visitRequestDTO), VisitRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VisitResponseDTO.class)
                .isEqualTo(visitResponseDTO);

        verify(visitService, times(1)).archiveCompletedVisit(anyString(), any(Mono.class));
    }

    @Test
    void whenCompletedVisitWithInvalidVisitId_ReturnNotFound() {
        String invalidVisitId = "invalidId";
        VisitRequestDTO visitRequestDTO = buildVisitRequestDTO(UUID.randomUUID().toString());

        when(visitService.archiveCompletedVisit(eq(invalidVisitId), any(Mono.class)))
                .thenReturn(Mono.error(new NotFoundException("No visit was found with visitId: " + invalidVisitId)));

        webTestClient
                .put()
                .uri("/visits/completed/" + invalidVisitId + "/archive")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(visitRequestDTO), VisitRequestDTO.class)
                .exchange()
                .expectStatus().isNotFound();

        verify(visitService, times(1)).archiveCompletedVisit(eq(invalidVisitId), any(Mono.class));
    }

    @Test
    void exportVisitsToCSV_ShouldReturnCSVFile() throws IOException {
        // Define the expected CSV content as a byte array with a predefined visit
        String expectedCsv = "VisitId,Description,VisitDate,PetId,PractitionerId,Status\n" +
                "1,\"Test Visit\",2024-10-17,123,456,ACTIVE\n";
        byte[] expectedContent = expectedCsv.getBytes(StandardCharsets.UTF_8);

        // Create a ByteArrayInputStream with the expected content
        ByteArrayInputStream csvData = new ByteArrayInputStream(expectedContent);

        when(visitService.exportVisitsToCSV()).thenReturn(Mono.just(new InputStreamResource(csvData)));

        webTestClient.get()
                .uri("/visits/export")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=visits.csv")
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectBody(byte[].class)
                .value(responseBody -> {
                    // Assert that the response body matches the expected content
                    assertArrayEquals(expectedContent, responseBody);
                });
        verify(visitService, times(1)).exportVisitsToCSV();
    }



    @Test
    void exportVisitsToCSV_ShouldReturnEmptyFile_WhenNoVisits() throws IOException {
        // Define the expected CSV content as a byte array with no  predefined visit
        String expectedCsv = "VisitId,Description,VisitDate,PetId,PractitionerId,Status\n";
        byte[] expectedContent = expectedCsv.getBytes(StandardCharsets.UTF_8);

        ByteArrayInputStream csvData = new ByteArrayInputStream(expectedContent);

        // Mock the service to return the InputStreamResource wrapped in a Mono
        when(visitService.exportVisitsToCSV()).thenReturn(Mono.just(new InputStreamResource(csvData)));

        webTestClient.get()
                .uri("/visits/export")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=visits.csv")
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectBody(byte[].class)
                .value(responseBody -> {
                    assertArrayEquals(expectedContent, responseBody);
                });
        verify(visitService, times(1)).exportVisitsToCSV();
    }


    @Test
    void exportVisitsToCSV_ShouldReturnServerError_WhenServiceFails() {
        when(visitService.exportVisitsToCSV()).thenReturn(Mono.error(new RuntimeException("Service failed")));

        webTestClient.get()
                .uri("/visits/export")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}

