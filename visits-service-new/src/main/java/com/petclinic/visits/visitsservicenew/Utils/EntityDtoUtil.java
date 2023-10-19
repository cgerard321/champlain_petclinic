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

@Component
@RequiredArgsConstructor
public class EntityDtoUtil {

    private final VetsClient vetsClient;
    private final PetsClient petsClient;

    public Mono<VisitResponseDTO> toVisitResponseDTO(Visit visit) {
        System.out.println("Entity Date in Mapping: " + visit.getVisitDate()); // Debugging

        Mono<PetResponseDTO> petResponseDTOMono = petsClient.getPetById(visit.getPetId());
        Mono<VetDTO> vetResponseDTOMono = vetsClient.getVetByVetId(visit.getPractitionerId());

        return Mono.zip(petResponseDTOMono, vetResponseDTOMono)
                .flatMap(tuple -> {
                    PetResponseDTO petResponseDTO = tuple.getT1();
                    VetDTO vetResponseDTO = tuple.getT2();

                    return Mono.just(VisitResponseDTO.builder()
                            .visitId(visit.getVisitId())
                            .visitDate(visit.getVisitDate())
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
                            .build());
                });
    }

    public Visit toVisitEntity(VisitRequestDTO visitRequestDTO) {
        Visit visit = new Visit();
        BeanUtils.copyProperties(visitRequestDTO, visit);
        return visit;
    }

    public String generateVisitIdString() {
        return UUID.randomUUID().toString();
    }
}
