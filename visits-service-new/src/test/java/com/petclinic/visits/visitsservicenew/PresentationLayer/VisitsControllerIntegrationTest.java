package com.petclinic.visits.visitsservicenew.PresentationLayer;
import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VisitsControllerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private VisitRepo visitRepo;

    @MockBean
    private VetsClient vetsClient;

    @MockBean
    private PetsClient petsClient;

    String uuidVisit1 = UUID.randomUUID().toString();
    String uuidVisit2 = UUID.randomUUID().toString();

    String uuidCancelledVisit1 = UUID.randomUUID().toString();
    String uuidCancelledVisit2 = UUID.randomUUID().toString();

    String uuidVet = UUID.randomUUID().toString();
    String uuidPet = UUID.randomUUID().toString();
    String uuidPhoto = UUID.randomUUID().toString();
    String uuidOwner = UUID.randomUUID().toString();

    private final String STATUS = "CONFIRMED";

    Set<SpecialtyDTO> set= new HashSet<>();
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

    Date currentDate =new Date();
    PetResponseDTO petResponseDTO = PetResponseDTO.builder()
            .petTypeId(uuidPet)
            .name("Billy")
            .birthDate(currentDate)
            .photoId(uuidPhoto)
            .ownerId(uuidOwner)
            .build();

    Visit visit1 = buildVisit(uuidVisit1,"this is a dummy description",vet.getVetId());
    Visit visit2 = buildVisit(uuidVisit2,"this is a dummy description",vet.getVetId());

    Visit cancelledVisit1 = buildCancelledVisit(uuidCancelledVisit1, "this is a dummy description", vet.getVetId());
    Visit cancelledVisit2 = buildCancelledVisit(uuidCancelledVisit2, "this is a dummy description", vet.getVetId());

    private final VisitResponseDTO visitResponseDTO = buildVisitResponseDto(visit1.getVisitId(),vet.getVetId());
    private final VisitRequestDTO visitRequestDTO = buildVisitRequestDto(vet.getVetId());

    private final String PRAC_ID = visitResponseDTO.getPractitionerId();
    private final String PET_ID = visitResponseDTO.getPetId();
    private final String VISIT_ID = visitResponseDTO.getVisitId();
    private final int dbSize = 4;
    //private final LocalDateTime visitDate = visitResponseDTO.getVisitDate().withSecond(0);


    @BeforeEach
    void dbSetUp(){
        Publisher<Visit> visitPublisher = visitRepo.deleteAll()
                .thenMany(visitRepo.save(visit1)
                        .thenMany(visitRepo.save(visit2)
                                .thenMany(visitRepo.save(cancelledVisit1)
                                        .thenMany(visitRepo.save(cancelledVisit2)))));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();
    }

    @Test
    void getAllVisits(){
        webTestClient
                .get()
                .uri("/visits")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list)-> assertEquals(list.size(), dbSize));
    }
    @Test
    void getVisitByVisitId(){
        webTestClient
                .get()
                .uri("/visits/"+visit1.getVisitId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit1.getVisitId())
                .jsonPath("$.practitionerId").isEqualTo(visit1.getPractitionerId())
                .jsonPath("$.petId").isEqualTo(visit1.getPetId())
                .jsonPath("$.description").isEqualTo(visit1.getDescription())
                .jsonPath("$.visitDate").isEqualTo("2024-11-25 13:45")
                .jsonPath("$.status").isEqualTo("UPCOMING");
    }
    @Test
    void getVisitByPractitionerId(){

        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        webTestClient
                .get()
                .uri("/visits/practitioner/"+vet.getVetId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list)->{
                    assertNotNull(list);
                    assertEquals(dbSize, list.size());
                    assertEquals(list.get(0).getVisitId(), visit1.getVisitId());
                    assertEquals(list.get(0).getPractitionerId(), visit1.getPractitionerId());
                    assertEquals(list.get(0).getPetId(), visit1.getPetId());
                    assertEquals(list.get(0).getDescription(), visit1.getDescription());
                    assertEquals(list.get(0).getVisitDate(), visit1.getVisitDate());
                    assertEquals(list.get(0).getStatus(), visit1.getStatus());
                });
    }

    @Test
    void getVisitsForPet(){
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));

        webTestClient
                .get()
                .uri("/visits/pets/"+visit1.getPetId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list)->{
                    assertNotNull(list);
                    assertEquals(dbSize, list.size());
                    assertEquals(list.get(0).getVisitId(), visit1.getVisitId());
                    assertEquals(list.get(0).getPractitionerId(), visit1.getPractitionerId());
                    assertEquals(list.get(0).getPetId(), visit1.getPetId());
                    assertEquals(list.get(0).getDescription(), visit1.getDescription());
                    assertEquals(list.get(0).getVisitDate(), visit1.getVisitDate());
                    assertEquals(list.get(0).getStatus(), visit1.getStatus());
                });
    }

    @Test
    void getVisitsForStatus(){

        visit1.setStatus(Status.CONFIRMED);

        visitRepo.save(visit1).block(); //block is telling the test to wait for the response to complete

        webTestClient
                .get()
                .uri("/visits/status/"+STATUS)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list)->{
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals(list.get(0).getVisitId(), visit1.getVisitId());
                    assertEquals(list.get(0).getPractitionerId(), visit1.getPractitionerId());
                    assertEquals(list.get(0).getPetId(), visit1.getPetId());
                    assertEquals(list.get(0).getDescription(), visit1.getDescription());
                    assertEquals(list.get(0).getVisitDate(), visit1.getVisitDate());
                    assertEquals(list.get(0).getStatus().toString(), "CONFIRMED");
                });
    }


    @Test
    void addVisit(){
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        VisitRequestDTO visitRequestDTO = new VisitRequestDTO();
        visitRequestDTO.setPractitionerId(visit1.getPractitionerId());
        visitRequestDTO.setPetId(visit1.getPetId());
        visitRequestDTO.setDescription(visit1.getDescription());
        visitRequestDTO.setVisitDate(LocalDateTime.parse("2024-11-25 13:45",DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visitRequestDTO.setStatus(Status.UPCOMING);

        webTestClient
                .post()
                .uri("/visits")
                .body(Mono.just(visitRequestDTO), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VisitResponseDTO.class)
                .value((visitDTO1) -> {
                    assertEquals(visitDTO1.getVisitDate(), LocalDateTime.parse("2024-11-25 13:45",DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    assertEquals(visitDTO1.getDescription(), visit1.getDescription());
                    assertEquals(visitDTO1.getPetId(), visit1.getPetId());
                    assertEquals(visitDTO1.getPractitionerId(), visit1.getPractitionerId());
                    assertEquals(visitDTO1.getStatus(), visit1.getStatus());
                });
    }



    @Test
    void updateVisit(){

        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));


        webTestClient
                .put()
                .uri("/visits/"+visit1.getVisitId())
                .body(Mono.just(visitResponseDTO), VisitResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit1.getVisitId())
                .jsonPath("$.practitionerId").isEqualTo(visit1.getPractitionerId())
                .jsonPath("$.petId").isEqualTo(visit1.getPetId())
                .jsonPath("$.description").isEqualTo(visit1.getDescription())
                .jsonPath("$.visitDate").isEqualTo("2024-11-25 13:45")
                .jsonPath("$.status").isEqualTo("UPCOMING");
    }

    @Test
    void updateStatusForVisitByVisitId(){
        String status = "CANCELLED";
        webTestClient
                .put()
                .uri("/visits/"+VISIT_ID+"/status/"+status)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit1.getVisitId())
                .jsonPath("$.practitionerId").isEqualTo(visit1.getPractitionerId())
                .jsonPath("$.petId").isEqualTo(visit1.getPetId())
                .jsonPath("$.description").isEqualTo(visit1.getDescription())
                .jsonPath("$.visitDate").isEqualTo("2024-11-25 13:45")
                .jsonPath("$.status").isEqualTo("CANCELLED");
    }

    @Test
    void deleteVisit(){
        webTestClient
                .delete()
                .uri("/visits/"+visit1.getVisitId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteAllCanceledVisits() {

        webTestClient
                .delete()
                .uri("/visits/cancelled")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        // Verify that canceled visits were deleted
        String cancelledStatus = "CANCELLED";
        StepVerifier.create(visitRepo.findAllByStatus(cancelledStatus))
                .expectNextCount(0)
                .verifyComplete();
    }


    private Visit buildVisit(String uuid,String description, String vetId){
        return Visit.builder()
                .visitId(uuid)

                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description(description)
                .petId("2")
                .practitionerId(vetId)
                .status(Status.UPCOMING)
                .build();
    }

    private Visit buildCancelledVisit(String uuid,String description, String vetId){
        return Visit.builder()
                .visitId(uuid)

                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description(description)
                .petId("2")
                .practitionerId(vetId)
                .status(Status.CANCELLED)
                .build();
    }

    private VisitResponseDTO buildVisitResponseDto(String visitId,String vetId){
        return VisitResponseDTO.builder()
                .visitId(visitId)
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .practitionerId(vetId)
                .status(Status.UPCOMING)
                .build();
    }
    private VisitRequestDTO buildVisitRequestDto(String vetId){
        return VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .practitionerId(vetId)
                .status(Status.UPCOMING)
                .build();
    }
}