package com.petclinic.visits.visitsservicenew.Utils;


import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetsClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Change one type to another
 */
@Component
@RequiredArgsConstructor
public class EntityDtoUtil {

    private final VetsClient vetsClient;
    private final PetsClient petsClient;

    /**
     * Transform a visit into a Mono<VisitResponseDTO>
     * @param visit The visit to transform
     */
    public Mono<VisitResponseDTO> toVisitResponseDTO(Visit visit) {
       // System.out.println("Entity Date in Mapping: " + visit.getVisitDate()); // Debugging

        Mono<PetResponseDTO> petResponseDTOMono = petsClient.getPetById(visit.getPetId());
        Mono<VetDTO> vetResponseDTOMono = vetsClient.getVetByVetId(visit.getPractitionerId());

        return Mono.zip(petResponseDTOMono, vetResponseDTOMono)
                .flatMap(tuple -> {
                    PetResponseDTO petResponseDTO = tuple.getT1();
                    VetDTO vetResponseDTO = tuple.getT2();

                    return Mono.just(VisitResponseDTO.builder()
                            .visitId(visit.getVisitId())
                            .visitStartDate(visit.getVisitStartDate())
                            .description(visit.getDescription())
                            .petId(visit.getPetId())
                            .petName(petResponseDTO.getName())
                            .petBirthDate(petResponseDTO.getBirthDate())
                            .practitionerId(visit.getPractitionerId())
                            .vetFirstName(vetResponseDTO.getFirstName())
                            .vetLastName(vetResponseDTO.getLastName())
                            .vetEmail(vetResponseDTO.getEmail())
                            .vetPhoneNumber(vetResponseDTO.getPhoneNumber())
                            .status(visit.getStatus())
                            .visitEndDate(visit.getVisitStartDate().plusHours(1))
                            .build());
                });
    }

    /**
     * Transform a Request DTO into a Visit
     * @param visitRequestDTO The DTO to transform
     * @return The transformed DTO into a Visit
     */
    public Visit toVisitEntity(VisitRequestDTO visitRequestDTO) {
        Visit visit = new Visit();
        BeanUtils.copyProperties(visitRequestDTO, visit);
        return visit;
    }

    /**
     * Generate a random UUID and returns it. IS NOT ERROR FREEa
     * @return The UUID as a string
     */
    public String generateVisitIdString() {
        return UUID.randomUUID().toString();
    }
}
