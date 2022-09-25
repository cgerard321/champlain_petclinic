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

    //Flux<VisitDTO> getVisitsForPet(int petId, boolean scheduled); //im pretty sure this is redundant unless there is something that only returns the active visits, not gonna touch it for now unless we need it

    Mono<VisitDTO> getVisitByVisitId(String visitId); //this is to get things by visitIds

    Mono<Void> deleteVisit(String visitId); //to delete the visit

    Mono<VisitDTO> updateVisit(String visitId, Mono<VisitDTO> visitDTOMono); //this is to update a visit/change it

    //Flux<VisitIdLessDTO> getVisitsForPets(List<VisitIdLessDTO> petIds); //I think this is a list of visits by the pet Id

    Flux<VisitDTO> getVisitsForPractitioner(int practitionerId); //this is to get the practitioner by their Id

    Flux<VisitDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, Date practitionerMonth); //this is the same thing as the one on top but its also by month

    //Boolean validateVisitId(String visitId); //ZERO clue what this does, at the moment im not gonna touch this
}
