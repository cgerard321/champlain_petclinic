package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureWebTestClient
public class VisitServiceImplTest {

    @Autowired
    private VisitService visitService;

    @MockBean
    private VisitRepo visitRepo;

    Visit visit = buildVisit();

    VisitDTO visitDTO = buildVisitDto();

    int PRAC_ID = visitDTO.getPractitionerId();
    int PET_ID = visitDTO.getPetId();

    int MONTH = visitDTO.getMonth();
    String VISIT_ID = visitDTO.getVisitId();

    @Test
    void getVisitByVisitId(){

        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit));

        String visitId = visit.getVisitId();

        Mono<VisitDTO> visitDTOMono = visitService.getVisitByVisitId(visitId);

        StepVerifier
                .create(visitDTOMono)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit.getYear(), foundVisit.getYear());
                    assertEquals(visit.getMonth(), foundVisit.getMonth());
                    assertEquals(visit.getDay(), foundVisit.getDay());
                    assertEquals(visit.getDescription(), foundVisit.getDescription());
                    assertEquals(visit.getPetId(), foundVisit.getPetId());
                    assertEquals(visit.getPractitionerId(), foundVisit.getPractitionerId());
                }).verifyComplete();
    }
    @Test
    void getVisitByPractitionerId(){

        when(visitRepo.findVisitsByPractitionerId(anyInt())).thenReturn(Flux.just(visit));

        Flux<VisitDTO> visitDTOFlux = visitService.getVisitsForPractitioner(PRAC_ID);

        StepVerifier
                .create(visitDTOFlux)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit.getYear(), foundVisit.getYear());
                    assertEquals(visit.getMonth(), foundVisit.getMonth());
                    assertEquals(visit.getDay(), foundVisit.getDay());
                    assertEquals(visit.getDescription(), foundVisit.getDescription());
                    assertEquals(visit.getPetId(), foundVisit.getPetId());
                    assertEquals(visit.getPractitionerId(), foundVisit.getPractitionerId());
                }).verifyComplete();
    }

    @Test
    void getVisitsForPet(){

        when(visitRepo.findByPetId(anyInt())).thenReturn(Flux.just(visit));

        Flux<VisitDTO> visitDTOFlux = visitService.getVisitsForPet(PET_ID);

        StepVerifier
                .create(visitDTOFlux)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit.getYear(), foundVisit.getYear());
                    assertEquals(visit.getMonth(), foundVisit.getMonth());
                    assertEquals(visit.getDay(), foundVisit.getDay());
                    assertEquals(visit.getDescription(), foundVisit.getDescription());
                    assertEquals(visit.getPetId(), foundVisit.getPetId());
                    assertEquals(visit.getPractitionerId(), foundVisit.getPractitionerId());
                }).verifyComplete();

    }
    @Test
    void getVisitsByPractitionerIdAndMonth(){

        when(visitRepo.findVisitsByPractitionerIdAndMonth(anyInt(), anyInt())).thenReturn(Flux.just(visit));

        Flux<VisitDTO> visitDTOFlux = visitService.getVisitsByPractitionerIdAndMonth(PET_ID, MONTH);

        StepVerifier
                .create(visitDTOFlux)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit.getYear(), foundVisit.getYear());
                    assertEquals(visit.getMonth(), foundVisit.getMonth());
                    assertEquals(visit.getDay(), foundVisit.getDay());
                    assertEquals(visit.getDescription(), foundVisit.getDescription());
                    assertEquals(visit.getPetId(), foundVisit.getPetId());
                    assertEquals(visit.getPractitionerId(), foundVisit.getPractitionerId());
                }).verifyComplete();

    }
    @Test
    void addVisit(){

        visitService.addVisit(Mono.just(visitDTO))
                .map(visitDTO1 -> {

                    assertEquals(visitDTO1.getVisitId(), visitDTO1.getVisitId());
                    assertEquals(visitDTO1.getDescription(), visitDTO1.getDescription());
                    assertEquals(visitDTO1.getPetId(), visitDTO1.getPetId());
                    assertEquals(visitDTO1.getDay(), visitDTO1.getDay());
                    assertEquals(visitDTO1.getMonth(), visitDTO1.getMonth());
                    assertEquals(visitDTO1.getYear(), visitDTO1.getYear());
                    assertEquals(visitDTO1.getPractitionerId(), visitDTO1.getPractitionerId());
                    return visitDTO1;
                });


    }
    @Test
    public void deleteVisit(){

        visitService.deleteVisit(VISIT_ID);
        verify(visitRepo, times(1)).deleteVisitByVisitId(VISIT_ID);

    }
    @Test
    void updateVisit(){

        when(visitRepo.save(any(Visit.class))).thenReturn(Mono.just(visit));

        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit));

        visitService.updateVisit(VISIT_ID, Mono.just(visitDTO))
                .map((visitDTO1) -> {

                    assertEquals(visitDTO1.getVisitId(), visitDTO1.getVisitId());
                    assertEquals(visitDTO1.getDescription(), visitDTO1.getDescription());
                    assertEquals(visitDTO1.getPetId(), visitDTO1.getPetId());
                    assertEquals(visitDTO1.getDay(), visitDTO1.getDay());
                    assertEquals(visitDTO1.getMonth(), visitDTO1.getMonth());
                    assertEquals(visitDTO1.getYear(), visitDTO1.getYear());
                    assertEquals(visitDTO1.getPractitionerId(), visitDTO1.getPractitionerId());
                    return visitDTO1;
                });

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
