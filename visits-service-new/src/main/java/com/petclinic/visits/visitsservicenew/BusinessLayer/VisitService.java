package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Simple interface for all the request controller. Implemented in VisitServiceImpl
 */
public interface VisitService {
    Flux<VisitResponseDTO> getAllVisits();
    Flux<VisitResponseDTO> getVisitsForPet(String petId);
    Flux<VisitResponseDTO> getVisitsForStatus(String statusString);
    Flux<VisitResponseDTO> getVisitsForPractitioner(String vetId);
    //Flux<VisitResponseDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, int month);
    Mono<VisitResponseDTO> getVisitByVisitId(String visitId);
    Mono<VisitResponseDTO> addVisit(Mono<VisitRequestDTO> visitRequestDTOMono);
    Mono<VisitResponseDTO> updateVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTOMono);
    Mono<VisitResponseDTO> updateStatusForVisitByVisitId(String visitId, String status);
    Mono<Void> deleteVisit(String visitId);
    Mono<Void> deleteAllCancelledVisits();
    Mono<Void>deleteCompletedVisitByVisitId(String visitId);

//    Mono<VetDTO> testingGetVetDTO(String vetId);
//    Mono<PetResponseDTO> testingGetPetDTO(int petId);

}
