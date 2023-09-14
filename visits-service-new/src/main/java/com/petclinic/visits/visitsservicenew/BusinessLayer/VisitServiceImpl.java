package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class VisitServiceImpl implements VisitService {

    @Autowired
    private VisitRepo repo;


    @Override
    public Mono<VisitDTO> addVisit(Mono<VisitDTO> visitIdLessDTOMono) {
        return visitIdLessDTOMono
                .map(EntityDtoUtil::toEntity)
                .doOnNext(x -> x.setVisitId(EntityDtoUtil.generateVisitIdString()))
                .flatMap((repo::save))
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Flux<VisitDTO> getVisitsForPet(int petId) {
        return repo.findByPetId(petId)
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Mono<VisitDTO> getVisitByVisitId(String visitId) {
        return repo.findByVisitId(visitId)
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Mono<Void> deleteVisit(String visitId) {
        return repo.deleteVisitByVisitId(visitId);
    }

    @Override
    public Mono<VisitDTO> updateVisit(String visitId, Mono<VisitDTO> visitDTOMono) {
        return repo.findByVisitId(visitId)
                .flatMap(v -> visitDTOMono
                        .map(EntityDtoUtil::toEntity)
                        .doOnNext(e->e.setVisitId(e.getVisitId()))
                        .doOnNext(e->e.setId(e.getId()))
                )
                .flatMap(repo::save)
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Flux<VisitDTO> getVisitsForPractitioner(int practitionerId) {
        return repo.findVisitsByPractitionerId(practitionerId)
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Flux<VisitDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, int month) {
        return repo.findVisitsByPractitionerIdAndMonth(practitionerId, month)
                .map(EntityDtoUtil::toDTO);
    }
}
