package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static reactor.core.publisher.Mono.when;

@SpringBootTest
@AutoConfigureWebTestClient
public class VisitServiceImplTest {

    @Autowired
    private VisitService visitService;

    @MockBean
    private VisitRepo visitRepo;

    @Test
    void getVisitByVisitId(){
        Visit visit = buildVisit();

        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit));

        String visitId = visit.getVisitId();

        Mono<VisitDTO> visitDTOMono = visitService.getVisitByVisitId(visitId);

        StepVerifier
                .create(visitDTOMono)
                .consumeNextWith(foundProduct -> {
                    assertEquals(visit.getVisitId(), foundProduct.getVisitId());
                    assertEquals(visit.getYear(), foundProduct.getYear());
                    assertEquals(visit.getMonth(), foundProduct.getMonth());
                    assertEquals(visit.getDay(), foundProduct.getDay());
                    assertEquals(visit.getDescription(), foundProduct.getDescription());
                    assertEquals(visit.getPetId(), foundProduct.getPetId());
                    assertEquals(visit.getPractitionerId(), foundProduct.getPractitionerId());
                }).verifyComplete();
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

}
