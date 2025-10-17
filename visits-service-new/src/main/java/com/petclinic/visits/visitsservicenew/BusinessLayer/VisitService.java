package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import org.springframework.core.io.InputStreamResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Simple interface for all the request controller. Implemented in VisitServiceImpl
 */
public interface VisitService {
    Flux<VisitResponseDTO> getAllVisits(String description);

    Flux<VisitResponseDTO> getVisitsForPet(String petId);

    Flux<VisitResponseDTO> getVisitsForStatus(String statusString);

    Flux<VisitResponseDTO> getVisitsForPractitioner(String vetId);

    //Flux<VisitResponseDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, int month);
    Mono<VisitResponseDTO> getVisitByVisitId(String visitId, boolean includePrescription);

    Mono<VisitResponseDTO> addVisit(Mono<VisitRequestDTO> visitRequestDTOMono);

    Mono<VisitResponseDTO> updateVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTOMono);

    Mono<VisitResponseDTO> updateStatusForVisitByVisitId(String visitId, String status);

    Mono<Void> deleteVisit(String visitId);

    Mono<Void> deleteAllCancelledVisits();


    Mono<VisitResponseDTO> patchVisitStatusInVisit(String visitId, String status);

    Mono<VisitResponseDTO> archiveCompletedVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTO);

    Flux<VisitResponseDTO> getAllArchivedVisits();

    Mono<InputStreamResource> exportVisitsToCSV();

//    Mono<VetDTO> testingGetVetDTO(String vetId);
//    Mono<PetResponseDTO> testingGetPetDTO(int petId);

}
