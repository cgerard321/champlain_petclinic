package com.petclinic.visits.visitsservicenew.PresentationLayer;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VisitsControllerIntegrationTest {
    @Autowired
    private WebTestClient client;

    @Autowired
    private VisitRepo visitRepo;

    @MockBean
    private VetsClient vetsClient;

    @MockBean
    private PetsClient petsClient;

    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    String uuid3 = UUID.randomUUID().toString();
    String uuid4 = UUID.randomUUID().toString();
    String uuid5 = UUID.randomUUID().toString();
    String uuid6 = UUID.randomUUID().toString();

    Set<SpecialtyDTO> set= new HashSet<>();

    VetDTO vet = VetDTO.builder()
            .vetId(uuid3)
            .vetBillId("1")
            .firstName("James")
            .lastName("Carter")
            .email("carter.james@email.com")
            .phoneNumber("(514)-634-8276 #2384")
            .imageId("1")
            .resume("Practicing since 3 years")
            .workday("Monday, Tuesday, Friday")
            .active(true)
            .specialties(set)
            .build();

    Date currentDate =new Date();
    PetResponseDTO petResponseDTO = PetResponseDTO.builder()
            .petTypeId(uuid4)
            .name("Billy")
            .birthDate(currentDate)
            .photoId(uuid5)
            .ownerId(uuid6)
            .build();

    Visit visit1 = buildVisit(uuid1,"this is a dummy description",vet.getVetId());
    Visit visit2 = buildVisit(uuid2,"this is a dummy description",vet.getVetId());
    private final VisitResponseDTO visitResponseDTO = buildVisitResponseDto(visit1.getVisitId(),vet.getVetId());
    private final VisitRequestDTO visitRequestDTO = buildVisitRequestDto(vet.getVetId());

    private final String PRAC_ID = visitResponseDTO.getPractitionerId();
    private final int PET_ID = visitResponseDTO.getPetId();
    private final String VISIT_ID = visitResponseDTO.getVisitId();
    private final int dbSize = 2;
    //private final LocalDateTime visitDate = visitResponseDTO.getVisitDate().withSecond(0);


    @BeforeEach
    void dbSetUp(){
        Publisher<Visit> visitPublisher = visitRepo.deleteAll()
                .thenMany(visitRepo.save(visit1)
                        .thenMany(visitRepo.save(visit2)));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();
    }

    @Test
    void getAllVisits(){
        client
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
        client
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
                .jsonPath("$.visitDate").isEqualTo("2022-11-25T13:45:00")
                .jsonPath("$.status").isEqualTo(visit1.isStatus());
    }
    @Test
    void getVisitByPractitionerId(){

        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        client
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
                    assertEquals(list.get(0).isStatus(), visit1.isStatus());
                });
    }

    @Test
    void getVisitsForPet(){
        when(petsClient.getPetById(anyInt())).thenReturn(Mono.just(petResponseDTO));

        client
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
                    assertEquals(list.get(0).isStatus(), visit1.isStatus());
                });
    }
    /*
    @Test
    void getVisitsByPractitionerIdAndMonth(){
        client
                .get()
                .uri("/visits/practitioner/"+PRAC_ID + "/" + MONTH)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list)->{
                    assertNotNull(list);
                    assertEquals(dbSize, list.size());
                    assertEquals(list.get(0).getVisitId(), visit.getVisitId());
                    assertEquals(list.get(0).getPractitionerId(), visit.getPractitionerId());
                    assertEquals(list.get(0).getPetId(), visit.getPetId());
                    assertEquals(list.get(0).getDescription(), visit.getDescription());
                    assertEquals(list.get(0).getDay(), visit.getDay());
                    assertEquals(list.get(0).getYear(), visit.getYear());
                    assertEquals(list.get(0).getMonth(), visit.getMonth());
                    assertEquals(list.get(0).isStatus(), visit.isStatus());
                });
    }
     */

    @Test
    void addVisit(){
        when(petsClient.getPetById(anyInt())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        client
                .post()
                .uri("/visits")
                .body(Mono.just(visitRequestDTO), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VisitResponseDTO.class)
                .value((visitDTO1) -> {
                    assertEquals(visitDTO1.getDescription(), visit1.getDescription());
                    assertEquals(visitDTO1.getPetId(), visit1.getPetId());
                    assertEquals(visitDTO1.getVisitDate(), visit1.getVisitDate());
                    assertEquals(visitDTO1.getPractitionerId(), visit1.getPractitionerId());
                });
    }
    @Test
    void deleteVisit(){
        client
                .delete()
                .uri("/visits/"+visit1.getVisitId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }
    @Test
    void updateVisit(){
        when(petsClient.getPetById(anyInt())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        client
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
                .jsonPath("$.visitDate").isEqualTo("2022-11-25T13:45:00")
                .jsonPath("$.status").isEqualTo(visit1.isStatus());
    }

    private Visit buildVisit(String uuid,String description, String vetId){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return Visit.builder()
                .visitId(uuid)
                .visitDate(LocalDateTime.parse("2022-11-25T13:45", dtf))
                .description(description)
                .petId(2)
                .practitionerId(vetId)
                .status(true).build();
    }

    private VisitResponseDTO buildVisitResponseDto(String visitId,String vetId){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return VisitResponseDTO.builder()
                .visitId(visitId)
                .visitDate(LocalDateTime.parse("2022-11-25T13:45:00", dtf))
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(vetId)
                .status(true).build();
    }
    private VisitRequestDTO buildVisitRequestDto(String vetId){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2022-11-25T13:45:00", dtf))
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(vetId)
                .status(true).build();
    }
}