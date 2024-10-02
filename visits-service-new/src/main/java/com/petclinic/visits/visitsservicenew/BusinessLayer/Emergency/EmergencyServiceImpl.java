package com.petclinic.visits.visitsservicenew.BusinessLayer.Emergency;

import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.EmergencyRepository;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class EmergencyServiceImpl implements EmergencyService{

    private final EmergencyRepository emergencyRepository;

    public EmergencyServiceImpl(EmergencyRepository emergencyRepository) {
        this.emergencyRepository = emergencyRepository;
    }

    @Override
    public Flux<EmergencyResponseDTO> GetAllEmergencies() {
        return emergencyRepository.findAll().map(EntityDtoUtil::toEmergencyResponseDTO);
    }

    @Override
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
}
