package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@WebFluxTest(VisitService.class)
class VisitServiceImplTest {

    @Autowired
    private VisitService visitService;

    @MockBean
    private VisitRepo visitRepo;

    private final Visit visit = buildVisit();

    private final VisitResponseDTO visitResponseDTO = buildVisitResponseDTO();
    private final VisitRequestDTO visitRequestDTO = buildVisitRequestDTO();
    private final int PRAC_ID = visitResponseDTO.getPractitionerId();
    private final int PET_ID = visitResponseDTO.getPetId();
    private final String VISIT_ID = visitResponseDTO.getVisitId();

    @Test
    void getVisitByVisitId(){
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit));

        String visitId = visit.getVisitId();

        Mono<VisitResponseDTO> visitResponseDTOMono = visitService.getVisitByVisitId(visitId);

        StepVerifier
                .create(visitResponseDTOMono)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit.getVisitDate(), foundVisit.getVisitDate());
                    assertEquals(visit.getDescription(), foundVisit.getDescription());
                    assertEquals(visit.getPetId(), foundVisit.getPetId());
                    assertEquals(visit.getPractitionerId(), foundVisit.getPractitionerId());
                }).verifyComplete();
    }
    @Test
    void getVisitsByPractitionerId(){
        when(visitRepo.findVisitsByPractitionerId(anyInt())).thenReturn(Flux.just(visit));

        Flux<VisitResponseDTO> visitResponseDTOFlux = visitService.getVisitsForPractitioner(PRAC_ID);

        StepVerifier
                .create(visitResponseDTOFlux)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit.getVisitDate(), foundVisit.getVisitDate());
                    assertEquals(visit.getDescription(), foundVisit.getDescription());
                    assertEquals(visit.getPetId(), foundVisit.getPetId());
                    assertEquals(visit.getPractitionerId(), foundVisit.getPractitionerId());
                }).verifyComplete();
    }

    @Test
    void getVisitsForPet(){
        when(visitRepo.findByPetId(anyInt())).thenReturn(Flux.just(visit));

        Flux<VisitResponseDTO> visitResponseDTOFlux = visitService.getVisitsForPet(PET_ID);

        StepVerifier
                .create(visitResponseDTOFlux)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit.getVisitDate(), foundVisit.getVisitDate());
                    assertEquals(visit.getDescription(), foundVisit.getDescription());
                    assertEquals(visit.getPetId(), foundVisit.getPetId());
                    assertEquals(visit.getPractitionerId(), foundVisit.getPractitionerId());
                }).verifyComplete();
    }

    @Test
    void getVisitsForStatus(){
        when(visitRepo.findAllByStatus(anyString())).thenReturn(Flux.just(visit));

        Flux<VisitResponseDTO> visitResponseDTOFlux = visitService.getVisitsForStatus(anyString());

        StepVerifier
                .create(visitResponseDTOFlux)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit.getVisitDate(), foundVisit.getVisitDate());
                    assertEquals(visit.getDescription(), foundVisit.getDescription());
                    assertEquals(visit.getPetId(), foundVisit.getPetId());
                    assertEquals(visit.getPractitionerId(), foundVisit.getPractitionerId());
                    assertEquals(visit.getStatus(), foundVisit.getStatus());
                }).verifyComplete();
    }

    /*
    @Test
    void getVisitsByPractitionerIdAndMonth(){
        when(visitRepo.findVisitsByPractitionerIdAndMonth(anyInt(), anyInt())).thenReturn(Flux.just(visit));

        Flux<VisitResponseDTO> visitDTOFlux = visitService.getVisitsByPractitionerIdAndMonth(PET_ID, MONTH);

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
     */

    @Test
    void addVisit(){
        when(visitRepo.insert(any(Visit.class))).thenReturn(Mono.just(visit));
        StepVerifier.create(visitService.addVisit(Mono.just(visitRequestDTO)))
                .consumeNextWith(visitDTO1 -> {
                    assertEquals(visit.getVisitId(), visitDTO1.getVisitId());
                    assertEquals(visit.getDescription(), visitDTO1.getDescription());
                    assertEquals(visit.getPetId(), visitDTO1.getPetId());
                    assertEquals(visit.getVisitDate(), visitDTO1.getVisitDate());
                    assertEquals(visit.getPractitionerId(), visitDTO1.getPractitionerId());
                }).verifyComplete();
    }
    @Test
    void deleteVisit(){
        visitService.deleteVisit(VISIT_ID);

        verify(visitRepo, times(1)).deleteVisitByVisitId(VISIT_ID);
    }
    @Test
    void updateVisit(){
        when(visitRepo.save(any(Visit.class))).thenReturn(Mono.just(visit));
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit));

        StepVerifier.create(visitService.updateVisit(VISIT_ID, Mono.just(visitRequestDTO)))
                .consumeNextWith(visitDTO1 -> {
                    assertEquals(visit.getVisitId(), visitDTO1.getVisitId());
                    assertEquals(visit.getDescription(), visitDTO1.getDescription());
                    assertEquals(visit.getPetId(), visitDTO1.getPetId());
                    assertEquals(visit.getVisitDate(), visitDTO1.getVisitDate());
                    assertEquals(visit.getPractitionerId(), visitDTO1.getPractitionerId());
                }).verifyComplete();
    }

    @Test
    void updateStatusForVisitByVisitId(){
        String status = "CANCELLED";

        when(visitRepo.save(any(Visit.class))).thenReturn(Mono.just(visit));
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit));

        StepVerifier.create(visitService.updateStatusForVisitByVisitId(VISIT_ID, status))
                .consumeNextWith(visitDTO1 -> {
                    assertEquals(visit.getVisitId(), visitDTO1.getVisitId());
                    assertEquals(visit.getDescription(), visitDTO1.getDescription());
                    assertEquals(visit.getPetId(), visitDTO1.getPetId());
                    assertEquals(visit.getVisitDate(), visitDTO1.getVisitDate());
                    assertEquals(visit.getPractitionerId(), visitDTO1.getPractitionerId());
                    assertEquals(visit.getStatus(), Status.CANCELLED);
                }).verifyComplete();
    }


    private Visit buildVisit(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return Visit.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2022-11-25T13:45", dtf))
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(Status.COMPLETED).build();
    }
    private VisitResponseDTO buildVisitResponseDTO(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2022-11-25T13:45:00", dtf))
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(Status.COMPLETED).build();
    }
    private VisitRequestDTO buildVisitRequestDTO() {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            return VisitRequestDTO.builder()
                    .visitDate(LocalDateTime.parse("2022-11-25T13:45:00", dtf))
                    .description("this is a dummy description")
                    .petId(2)
                    .practitionerId(2)
                    .status(Status.COMPLETED).build();
        }

}
