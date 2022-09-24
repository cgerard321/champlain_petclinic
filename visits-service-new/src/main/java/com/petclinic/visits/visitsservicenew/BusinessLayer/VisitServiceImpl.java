package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitIdLessDTO;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Service
public class VisitServiceImpl implements VisitService {

    @Autowired
    VisitRepo repo;


    @Override
    public Mono<VisitIdLessDTO> addVisit(Mono<VisitIdLessDTO> visitIdLessDTOMono) {
        return visitIdLessDTOMono
                .map(EntityDtoUtil::IdlesstoEntity)
                .flatMap(repo::save)
                .map(EntityDtoUtil::toIdLessDTO);
    }

    @Override
    public Flux<VisitDTO> getVisitsForPet(int petId) {
        return repo.findByPetId(petId)
                .map(EntityDtoUtil::toDTO);
    }

    /*@Override //check the VisitService comments
    public Flux<VisitDTO> getVisitsForPet(int petId, boolean scheduled) {
        return null;
    }*/

    @Override
    public Mono<VisitDTO> getVisitByVisitId(String visitId) {
        return repo.findByVisitId(UUID.fromString(visitId))
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Mono<Void> deleteVisit(String visitId) {
        return repo.findByVisitId(UUID.fromString(visitId))
                .flatMap(repo::delete);
    }

    @Override
    public Mono<VisitDTO> updateVisit(String visitId, Mono<VisitDTO> visitDTOMono) {
        return repo.findByVisitId(UUID.fromString(visitId))
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
    public Flux<VisitDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, Date practitionerMonth) {
        Calendar date = Calendar.getInstance();
        date.setTime(practitionerMonth);
        return repo.findVisitsByPractitionerIdAndMonth(practitionerId, date.get(Calendar.MONTH))
                .map(EntityDtoUtil::toDTO);
    }





    /*@Override
    public Boolean validateVisitId(String visitId) { //ive got zero idea what this does if somebody knows PLEASE tell me
        String uuidRegex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
        if(visitId.matches(uuidRegex))
            return true;
        return false;
    }*/
}
