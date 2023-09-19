package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface VisitService {

    Mono<VisitResponseDTO> addVisit(Mono<VisitRequestDTO> visitRequestDTOMono);

    Flux<VisitResponseDTO> getVisitsForPet(int petId);

    Mono<VisitResponseDTO> getVisitByVisitId(String visitId);

    Mono<Void> deleteVisit(String visitId);

    Mono<VisitResponseDTO> updateVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTOMono);

    Flux<VisitResponseDTO> getVisitsForPractitioner(int practitionerId);


    //Flux<VisitResponseDTO> getVisitsByPractitionerIdAndVisitDate(int practitionerId, LocalDateTime visitDate); replace with query params
}
