package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VisitService {
    Flux<VisitResponseDTO> getAllVisits();
    Flux<VisitResponseDTO> getVisitsForPet(int petId);
    Flux<VisitResponseDTO> getVisitsForStatus(String statusString);
    Flux<VisitResponseDTO> getVisitsForPractitioner(int practitionerId);
    //Flux<VisitResponseDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, int month);
    Mono<VisitResponseDTO> getVisitByVisitId(String visitId);
    Mono<VisitResponseDTO> addVisit(Mono<VisitRequestDTO> visitRequestDTOMono);
    Mono<VisitResponseDTO> updateVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTOMono);
    Mono<VisitResponseDTO> updateStatusForVisitByVisitId(String visitId, String status);
    Mono<Void> deleteVisit(String visitId);
}
