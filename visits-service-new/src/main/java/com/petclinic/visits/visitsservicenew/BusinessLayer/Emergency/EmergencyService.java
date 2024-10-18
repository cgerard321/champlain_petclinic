package com.petclinic.visits.visitsservicenew.BusinessLayer.Emergency;

import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyResponseDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewResponseDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmergencyService {

    Flux<EmergencyResponseDTO> GetAllEmergencies();

    Flux<EmergencyResponseDTO> getEmergencyVisitsForPet(String petId);

   Mono<EmergencyResponseDTO> AddEmergency(Mono<EmergencyRequestDTO> emergencyRequestDTOMono);
    /*Mono<EmergencyResponseDTO> UpdateEmergency(Mono<EmergencyRequestDTO> emergencyRequestDTOMono, String emergencyId);
    Mono<EmergencyResponseDTO> DeleteEmergency(String emergencyId);

     */

    Mono<EmergencyResponseDTO> GetEmergencyByEmergencyId(String emergencyId);
}
