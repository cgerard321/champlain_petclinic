package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {
    private final VisitRepo repo;

    @Override
    public Flux<VisitResponseDTO> getAllVisits(){
        return repo.findAll().map(EntityDtoUtil::toVisitResponseDTO);
    }
    @Override
    public Flux<VisitResponseDTO> getVisitsForPet(int petId) {
        return repo.findByPetId(petId)
                .map(EntityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Flux<VisitResponseDTO> getVisitsForStatus(String statusString) {
        Status status;
        switch (statusString){

            case("UPCOMING"):
                status = Status.UPCOMING;

            case("REQUESTED"):
                status = Status.REQUESTED;

            case("CONFIRMED"):
                status = Status.CONFIRMED;

            case("CANCELLED"):
                status = Status.CANCELLED;

            case("IN_PROGRESS"):
                status = Status.IN_PROGRESS;

            case("COMPLETED"):
                status = Status.COMPLETED;

            default:
                status = Status.CONFIRMED;
        }
        return repo.findAllByStatus(statusString)
                .map(EntityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Flux<VisitResponseDTO> getVisitsForPractitioner(int practitionerId) {
        return repo.findVisitsByPractitionerId(practitionerId)
                .map(EntityDtoUtil::toVisitResponseDTO);
    }
    /*
    @Override
    public Flux<VisitResponseDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, int month) {
        return repo.findVisitsByPractitionerIdAndMonth(practitionerId, month)
                .map(EntityDtoUtil::toVisitResponseDTO);
    }
     */
    @Override
    public Mono<VisitResponseDTO> getVisitByVisitId(String visitId) {
        return repo.findByVisitId(visitId)
                .map(EntityDtoUtil::toVisitResponseDTO);
    }
    @Override
    public Mono<VisitResponseDTO> addVisit(Mono<VisitRequestDTO> visitRequestDTOMono) {
        return visitRequestDTOMono
                .map(EntityDtoUtil::toVisitEntity)
                .doOnNext(x -> x.setVisitId(EntityDtoUtil.generateVisitIdString()))
                .flatMap((repo::insert))
                .map(EntityDtoUtil::toVisitResponseDTO);
    }
    @Override
    public Mono<Void> deleteVisit(String visitId) {
        return repo.deleteVisitByVisitId(visitId);
    }
    @Override
    public Mono<VisitResponseDTO> updateVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTOMono) {
        return repo.findByVisitId(visitId)
                .flatMap(v -> visitRequestDTOMono
                        .map(EntityDtoUtil::toVisitEntity)
                        .doOnNext(e->e.setVisitId(v.getVisitId()))
                        .doOnNext(e->e.setId(v.getId()))
                )
                .flatMap(repo::save)
                .map(EntityDtoUtil::toVisitResponseDTO);
    }


}