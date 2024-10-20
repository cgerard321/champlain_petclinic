package com.petclinic.visits.visitsservicenew.BusinessLayer.Emergency;

import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.Emergency;
import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.EmergencyRepository;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetsClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class EmergencyServiceImpl implements EmergencyService{
    private final VetsClient vetsClient;
    private final PetsClient petsClient;
    private final EmergencyRepository emergencyRepository;
    private final EntityDtoUtil entityDtoUtil;

    public EmergencyServiceImpl(VetsClient vetsClient, PetsClient petsClient, EmergencyRepository emergencyRepository, EntityDtoUtil entityDtoUtil) {
        this.vetsClient = vetsClient;
        this.petsClient = petsClient;
        this.emergencyRepository = emergencyRepository;
        this.entityDtoUtil = entityDtoUtil;
    }

    private Mono<PetResponseDTO> validatePetId(String petId) {
        return petsClient.getPetById(petId)
                .switchIfEmpty(Mono.error(new NotFoundException("No pet was found with petId: " + petId)));
    }

    @Override
    public Flux<EmergencyResponseDTO> GetAllEmergencies() {
        Flux<Emergency> emergencyFlux;
        emergencyFlux= emergencyRepository.findAll();
        return emergencyFlux.flatMap(entityDtoUtil::toEmergencyResponseDTO);
    }

    @Override
    public Flux<EmergencyResponseDTO> getEmergencyVisitsForPet(String petId) {
        return validatePetId(petId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("No pet was found with petId: " + petId))))
                .thenMany(emergencyRepository.findByPetId(petId)
                        .flatMap(entityDtoUtil::toEmergencyResponseDTO));
    }

    @Override
    public Mono<EmergencyResponseDTO> AddEmergency(Mono<EmergencyRequestDTO> emergencyRequestDTOMono) {
        return emergencyRequestDTOMono
                .flatMap(emergencyRequestDTO -> {
                    // Validate the pet and vet IDs
                    Mono<PetResponseDTO> petResponseMono = validatePetId(emergencyRequestDTO.getPetId());
                    Mono<VetDTO> vetResponseMono = vetsClient.getVetByVetId(emergencyRequestDTO.getPractitionerId())
                            .switchIfEmpty(Mono.error(new NotFoundException("No vet found with practitionerId: " + emergencyRequestDTO.getPractitionerId())));

                    // Once both are validated, combine the results
                    return Mono.zip(petResponseMono, vetResponseMono)
                            .flatMap(tuple -> {
                                PetResponseDTO petResponse = tuple.getT1();
                                VetDTO vetResponse = tuple.getT2();

                                // Create an Emergency entity
                                Emergency emergency = Emergency.builder()
                                        .visitEmergencyId(UUID.randomUUID().toString())
                                        .visitDate(emergencyRequestDTO.getVisitDate())
                                        .description(emergencyRequestDTO.getDescription())
                                        .petId(emergencyRequestDTO.getPetId())
                                        .practitionerId(emergencyRequestDTO.getPractitionerId())
                                        .urgencyLevel(emergencyRequestDTO.getUrgencyLevel())
                                        .emergencyType(emergencyRequestDTO.getEmergencyType())
                                        .build();

                                // Save the Emergency entity
                                return emergencyRepository.save(emergency)
                                        .flatMap(savedEmergency -> entityDtoUtil.toEmergencyResponseDTO(savedEmergency));
                            });
                });
    }

    @Override
    public Mono<EmergencyResponseDTO> GetEmergencyByEmergencyId(String emergencyId) {
        return emergencyRepository.findEmergenciesByVisitEmergencyId(emergencyId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("No visit was found with visitId: " + emergencyId))))
                .doOnNext(visit -> log.debug("The Emergency visit entity is: " + visit.toString()))
                .flatMap(entityDtoUtil::toEmergencyResponseDTO);
    }

    /*@Override
    public Mono<EmergencyResponseDTO> AddEmergency(Mono<EmergencyRequestDTO> emergencyRequestDTOMono) {
        return emergencyRequestDTOMono
                .map(EntityDtoUtil::toEmergencyEntity)
                //.doOnNext(e-> e.setReviewId(EntityDtoUtil.generateReviewIdString()))
                .flatMap(emergencyRepository::save)
                .map(EntityDtoUtil::toEmergencyResponseDTO);

    }

    @Override
    public Mono<EmergencyResponseDTO> UpdateEmergency(Mono<EmergencyRequestDTO> emergencyRequestDTOMono, String emergencyId) {
        return emergencyRepository.findEmergenciesByVisitEmergencyId(emergencyId)
                .switchIfEmpty(Mono.defer(()-> Mono.error(new NotFoundException("emergency id is not found: "+ emergencyId))))
                .flatMap(found->emergencyRequestDTOMono
                        .map(EntityDtoUtil::toEmergencyEntity)
                        .doOnNext(e->e.setVisitEmergencyId(found.getVisitEmergencyId()))
                        .doOnNext(e->e.setId(found.getId())))
                .flatMap(emergencyRepository::save)
                .map(EntityDtoUtil::toEmergencyResponseDTO);
    }

    @Override
    public Mono<EmergencyResponseDTO> DeleteEmergency(String emergencyId) {
       return  emergencyRepository.findEmergenciesByVisitEmergencyId(emergencyId)
                 .switchIfEmpty(Mono.defer(()-> Mono.error(new NotFoundException("emergency id is not found: "+ emergencyId))))
                 .flatMap(found ->emergencyRepository.delete(found)
                         .then(Mono.just(found)))
                 .map(EntityDtoUtil::toEmergencyResponseDTO);


    }

    @Override
    public Mono<EmergencyResponseDTO> GetEmergencyByEmergencyId(String emergencyId) {
        return emergencyRepository.findEmergenciesByVisitEmergencyId(emergencyId)
                .switchIfEmpty(Mono.defer(()-> Mono.error(new NotFoundException("emergency id is not found: "+ emergencyId))))
                .doOnNext(c-> log.debug("the emergency entity is: " + c.toString()))
                .map(EntityDtoUtil::toEmergencyResponseDTO);
    }
     */
}
