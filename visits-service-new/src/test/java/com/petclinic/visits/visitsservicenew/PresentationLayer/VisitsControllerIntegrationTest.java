package com.petclinic.visits.visitsservicenew.PresentationLayer;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VisitsControllerIntegrationTest {
    @Autowired
    private WebTestClient client;

    @Autowired
    private VisitRepo visitRepo;

    private final Visit visit = buildVisit();
    private final VisitResponseDTO visitResponseDTO = buildVisitResponseDto();
    private final VisitRequestDTO visitRequestDTO = buildVisitRequestDto();

    private final int PRAC_ID = visitResponseDTO.getPractitionerId();
    private final int PET_ID = visitResponseDTO.getPetId();
    private final String VISIT_ID = visitResponseDTO.getVisitId();
    private final int dbSize = 1;
    //private final LocalDateTime visitDate = visitResponseDTO.getVisitDate().withSecond(0);


    @BeforeEach
    void dbSetUp(){
        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));
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
                .uri("/visits/"+VISIT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$.practitionerId").isEqualTo(visit.getPractitionerId())
                .jsonPath("$.petId").isEqualTo(visit.getPetId())
                .jsonPath("$.description").isEqualTo(visit.getDescription())
                .jsonPath("$.visitDate").isEqualTo("2022-11-25T13:45:00")
                .jsonPath("$.status").isEqualTo(visit.isStatus());
    }
    @Test
    void getVisitByPractitionerId(){
        client
                .get()
                .uri("/visits/practitioner/visits/"+PRAC_ID)
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
                    assertEquals(list.get(0).getVisitDate(), visit.getVisitDate());
                    assertEquals(list.get(0).isStatus(), visit.isStatus());
                });
    }

    @Test
    void getVisitsForPet(){
        client
                .get()
                .uri("/visits/pets/"+PET_ID)
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
                    assertEquals(list.get(0).getVisitDate(), visit.getVisitDate());
                    assertEquals(list.get(0).isStatus(), visit.isStatus());
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
                    assertEquals(visitDTO1.getDescription(), visit.getDescription());
                    assertEquals(visitDTO1.getPetId(), visit.getPetId());
                    assertEquals(visitDTO1.getVisitDate(), visit.getVisitDate());
                    assertEquals(visitDTO1.getPractitionerId(), visit.getPractitionerId());
                });
    }
    @Test
    void deleteVisit(){
        client
                .delete()
                .uri("/visits/"+VISIT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }
    @Test
    void updateVisit(){
        client
                .put()
                .uri("/visits/visits/"+VISIT_ID)
                .body(Mono.just(visitResponseDTO), VisitResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$.practitionerId").isEqualTo(visit.getPractitionerId())
                .jsonPath("$.petId").isEqualTo(visit.getPetId())
                .jsonPath("$.description").isEqualTo(visit.getDescription())
                .jsonPath("$.visitDate").isEqualTo("2022-11-25T13:45:00")
                .jsonPath("$.status").isEqualTo(visit.isStatus());
    }

    private Visit buildVisit(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return Visit.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2022-11-25T13:45:00", dtf))
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(true).build();
    }

    private VisitResponseDTO buildVisitResponseDto(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2022-11-25T13:45:00", dtf))
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(true).build();
    }
    private VisitRequestDTO buildVisitRequestDto(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2022-11-25T13:45:00", dtf))
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(true).build();
    }
}