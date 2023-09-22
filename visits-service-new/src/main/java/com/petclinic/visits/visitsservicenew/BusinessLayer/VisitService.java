package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VisitService {
    Flux<VisitResponseDTO> getAllVisits();
    Flux<VisitResponseDTO> getVisitsForPet(int petId);
    Flux<VisitResponseDTO> getVisitsForPractitioner(String vetId);
    //Flux<VisitResponseDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, int month);
    Mono<VisitResponseDTO> getVisitByVisitId(String visitId);
    Mono<VisitResponseDTO> addVisit(Mono<VisitRequestDTO> visitRequestDTOMono);
    Mono<VisitResponseDTO> updateVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTOMono);
    Mono<Void> deleteVisit(String visitId);
//    Mono<VetDTO> testingGetVetDTO(String vetId);
//    Mono<PetResponseDTO> testingGetPetDTO(int petId);
}
