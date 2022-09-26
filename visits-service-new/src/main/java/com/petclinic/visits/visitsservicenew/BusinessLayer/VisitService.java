package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitIdLessDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

@Service
public interface VisitService {

    //first comments by Josh

    Mono<VisitDTO> addVisit(Mono<VisitDTO> visit); //this is to add a visit it's in the name

    Flux<VisitDTO> getVisitsForPet(int petId); //im assuming this is to get the visits by the petId

    Mono<VisitDTO> getVisitByVisitId(String visitId); //this is to get things by visitIds

    Mono<Void> deleteVisit(String visitId); //to delete the visit

    Mono<VisitDTO> updateVisit(String visitId, Mono<VisitDTO> visitDTOMono); //this is to update a visit/change it

    Flux<VisitDTO> getVisitsForPractitioner(int practitionerId); //this is to get the practitioner by their Id

    Flux<VisitDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, int month); //this is the same thing as the one on top but its also by month
}
