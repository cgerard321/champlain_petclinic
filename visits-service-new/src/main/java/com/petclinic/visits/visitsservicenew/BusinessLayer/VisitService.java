package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitIdLessDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

public interface VisitService {

    Mono<VisitIdLessDTO> addVisit(Mono<VisitIdLessDTO> visit);

    Flux<VisitDTO> getVisitsForPet(int petId);

    Flux<VisitDTO> getVisitsForPet(int petId, boolean scheduled);

    Mono<VisitDTO> getVisitByVisitId(String visitId);

    Mono<Void> deleteVisit(String visitId);

    Mono<VisitDTO> updateVisit(String visitId, Mono<VisitDTO> visitDTOMono);

    Flux<VisitDTO> getVisitsForPets(List<Integer> petIds);

    Flux<VisitDTO> getVisitsForPractitioner(int practitionerId);

    Flux<VisitDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, Date startDate, Date EndDate);

    Boolean validateVisitId(String visitId);
}
