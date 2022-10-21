package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VisitService {

    Mono<VisitDTO> addVisit(Mono<VisitDTO> visit);

    Flux<VisitDTO> getVisitsForPet(int petId);

    Mono<VisitDTO> getVisitByVisitId(String visitId);

    Mono<Void> deleteVisit(String visitId);

    Mono<VisitDTO> updateVisit(String visitId, Mono<VisitDTO> visitDTOMono);

    Flux<VisitDTO> getVisitsForPractitioner(int practitionerId);

    Flux<VisitDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, int month);
}
