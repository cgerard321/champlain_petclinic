package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;

import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetsClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import com.petclinic.visits.visitsservicenew.Exceptions.BadRequestException;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {
    private final VisitRepo repo;
    private final VetsClient vetsClient;
    private final PetsClient petsClient;

    @Override
    public Flux<VisitResponseDTO> getAllVisits() {
        return repo.findAll().map(EntityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Flux<VisitResponseDTO> getVisitsForPet(String petId) {
        return validatePetId(petId)
                .thenMany(repo.findByPetId(petId)
                        .map(EntityDtoUtil::toVisitResponseDTO));
    }

    @Override
    public Flux<VisitResponseDTO> getVisitsForPractitioner(String vetId) {
        return validateVetId(vetId)
                .thenMany(repo.findVisitsByPractitionerId(vetId))
                .map(EntityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Mono<VisitResponseDTO> getVisitByVisitId(String visitId) {
        return repo.findByVisitId(visitId)
                .map(EntityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Mono<VisitResponseDTO> addVisit(Mono<VisitRequestDTO> visitRequestDTOMono) {
        return visitRequestDTOMono
                .flatMap(visitRequestDTO -> validateVisitRequest(visitRequestDTO)
                        .then(validatePetId(visitRequestDTO.getPetId()))
                        .then(validateVetId(visitRequestDTO.getPractitionerId()))
                        .then(Mono.just(visitRequestDTO))
                )
                .doOnNext(v -> System.out.println("Request Date: " + v.getVisitDate())) // Debugging
                .map(EntityDtoUtil::toVisitEntity)
                .doOnNext(x -> x.setVisitId(EntityDtoUtil.generateVisitIdString()))
                .doOnNext(v -> System.out.println("Entity Date: " + v.getVisitDate())) // Debugging
                .flatMap((repo::insert))
                .map(EntityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Mono<Void> deleteVisit(String visitId) {
        return repo.existsByVisitId(visitId)
                .flatMap(visitExists -> {
                    if (!visitExists) {
                        return Mono.error(new NotFoundException("No visit was found with visitId: " + visitId));
                    } else {
                        return repo.deleteByVisitId(visitId);
                    }
                });
    }


//    @Override
//    public Mono<VetDTO> testingGetVetDTO(String vetId) {
//        return vetsClient.getVetByVetId(vetId);
//    }
//
//    @Override
//    public Mono<PetResponseDTO> testingGetPetDTO(int petId) {
//        return petsClient.getPetById(petId);
//    }
//

    @Override
    public Mono<VisitResponseDTO> updateVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTOMono) {
        return repo.findByVisitId(visitId)
                .flatMap(visitEntity -> visitRequestDTOMono
                        .flatMap(visitRequestDTO -> validatePetId(visitRequestDTO.getPetId())
                                .then(validateVetId(visitRequestDTO.getPractitionerId()))
                                .then(Mono.just(visitRequestDTO)))
                        .map(EntityDtoUtil::toVisitEntity)
                        .doOnNext(visitEntityToUpdate -> {
                            visitEntityToUpdate.setVisitId(visitEntity.getVisitId());
                            visitEntityToUpdate.setId(visitEntity.getId());
                        }))
                .flatMap(repo::save)
                .map(EntityDtoUtil::toVisitResponseDTO);
    }


    private Mono<PetResponseDTO> validatePetId(String petId) {
        return petsClient.getPetById(petId)
                .switchIfEmpty(Mono.error(new NotFoundException("No pet was found with petId: " + petId)));
    }

    private Mono<VetDTO> validateVetId(String vetId) {
        return vetsClient.getVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet was found with vetId: " + vetId)));
    }

    private Mono<VisitRequestDTO> validateVisitRequest(VisitRequestDTO dto) {
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            return Mono.error(new BadRequestException("Please enter a description for this visit"));
        } else if (dto.getVisitDate() == null || dto.getVisitDate().isBefore(LocalDateTime.now())) {
            return Mono.error(new BadRequestException("Appointment cannot be scheduled in the past"));
        } else if (dto.getPetId() == null || dto.getPetId().isBlank()) {
            return Mono.error(new BadRequestException("PetId cannot be null or blank"));
        } else if ( dto.getPractitionerId() == null || dto.getPractitionerId().isBlank()) {
            return Mono.error(new BadRequestException("VetId cannot be null or blank"));
        } else {
            return Mono.just(dto);
        }
    }
}