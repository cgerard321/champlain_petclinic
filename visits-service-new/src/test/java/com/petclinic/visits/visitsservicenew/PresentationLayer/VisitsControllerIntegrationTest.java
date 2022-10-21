package com.petclinic.visits.visitsservicenew.PresentationLayer;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
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

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VisitsControllerIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    VisitRepo visitRepo;

    Visit visit = buildVisit();
    VisitDTO visitDTO = buildVisitDto();

    int PRAC_ID = visitDTO.getPractitionerId();
    int PET_ID = visitDTO.getPetId();
    int MONTH = visitDTO.getMonth();
    String VISIT_ID = visitDTO.getVisitId();

    @Test
    void getVisitByVisitId(){

        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

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
                .jsonPath("$.day").isEqualTo(visit.getDay())
                .jsonPath("$.year").isEqualTo(visit.getYear())
                .jsonPath("$.month").isEqualTo(visit.getMonth())
                .jsonPath("$.status").isEqualTo(visit.isStatus());
    }
    @Test
    void getVisitByPractitionerId(){

        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

        client
                .get()
                .uri("/visits/practitioner/visits/"+PRAC_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$[0].practitionerId").isEqualTo(visit.getPractitionerId())
                .jsonPath("$[0].petId").isEqualTo(visit.getPetId())
                .jsonPath("$[0].description").isEqualTo(visit.getDescription())
                .jsonPath("$[0].day").isEqualTo(visit.getDay())
                .jsonPath("$[0].year").isEqualTo(visit.getYear())
                .jsonPath("$[0].month").isEqualTo(visit.getMonth())
                .jsonPath("$[0].status").isEqualTo(visit.isStatus());

    }

    @Test
    void getVisitsForPet(){

        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

        client
                .get()
                .uri("/visits/pets/"+PET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$[0].practitionerId").isEqualTo(visit.getPractitionerId())
                .jsonPath("$[0].petId").isEqualTo(visit.getPetId())
                .jsonPath("$[0].description").isEqualTo(visit.getDescription())
                .jsonPath("$[0].day").isEqualTo(visit.getDay())
                .jsonPath("$[0].year").isEqualTo(visit.getYear())
                .jsonPath("$[0].month").isEqualTo(visit.getMonth())
                .jsonPath("$[0].status").isEqualTo(visit.isStatus());

    }
    @Test
    void getVisitsByPractitionerIdAndMonth(){

        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

        client
                .get()
                .uri("/visits/practitioner/"+PRAC_ID + "/" + MONTH)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$[0].practitionerId").isEqualTo(visit.getPractitionerId())
                .jsonPath("$[0].petId").isEqualTo(visit.getPetId())
                .jsonPath("$[0].description").isEqualTo(visit.getDescription())
                .jsonPath("$[0].day").isEqualTo(visit.getDay())
                .jsonPath("$[0].year").isEqualTo(visit.getYear())
                .jsonPath("$[0].month").isEqualTo(visit.getMonth())
                .jsonPath("$[0].status").isEqualTo(visit.isStatus());

    }
    @Test
    void addVisit(){

        Publisher<Void> visitPublisher = visitRepo.deleteAll();

        StepVerifier.create(visitPublisher).expectNextCount(0).verifyComplete();

        client
                .post()
                .uri("/visits")
                .body(Mono.just(visit), Visit.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VisitDTO.class)
                .value((visitDTO1) -> {
                    assertEquals(visitDTO1.getVisitId(), visitDTO1.getVisitId());
                    assertEquals(visitDTO1.getDescription(), visitDTO1.getDescription());
                    assertEquals(visitDTO1.getPetId(), visitDTO1.getPetId());
                    assertEquals(visitDTO1.getDay(), visitDTO1.getDay());
                    assertEquals(visitDTO1.getMonth(), visitDTO1.getMonth());
                    assertEquals(visitDTO1.getYear(), visitDTO1.getYear());
                    assertEquals(visitDTO1.getPractitionerId(), visitDTO1.getPractitionerId());
                });
    }
    @Test
    void deleteVisit(){

        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

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

        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

        client
                .put()
                .uri("/visits/visits/"+VISIT_ID)
                .body(Mono.just(visitDTO), VisitDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$.practitionerId").isEqualTo(visit.getPractitionerId())
                .jsonPath("$.petId").isEqualTo(visit.getPetId())
                .jsonPath("$.description").isEqualTo(visit.getDescription())
                .jsonPath("$.day").isEqualTo(visit.getDay())
                .jsonPath("$.year").isEqualTo(visit.getYear())
                .jsonPath("$.month").isEqualTo(visit.getMonth())
                .jsonPath("$.status").isEqualTo(visit.isStatus());

    }

    private Visit buildVisit(){

        return Visit.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .year(2022)
                .month(11)
                .day(24)
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(true).build();
    }
    private VisitDTO buildVisitDto(){

        return VisitDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .year(2022)
                .month(11)
                .day(24)
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(true).build();
    }
}
