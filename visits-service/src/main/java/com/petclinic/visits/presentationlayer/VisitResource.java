package com.petclinic.visits.presentationlayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Maciej Szarlinski
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Timed("petclinic.visit")
class VisitResource {

    private final VisitRepository visitRepository;


    //To create a new visits
    @PostMapping("owners/*/pets/{petId}/visits")
    @ResponseStatus(HttpStatus.CREATED)
    public Visit create(
            @Valid @RequestBody Visit visit,
            @PathVariable("petId") int petId) {

        visit.setPetId(petId);
        log.info("Saving visit {}", visit);
        return visitRepository.save(visit);
    }


    //To pull in my main
    @GetMapping("owners/*/pets/{petId}/visits")
    public List<Visit> visits(@PathVariable("petId") int petId) {
        return visitRepository.findByPetId(petId);
    }


    //This method will return every visits of people that have multiple pets
    @GetMapping("pets/visits")
    public Visits visitsMultiGet(@RequestParam("petId") List<Integer> petIds) {
        final List<Visit> byPetIdIn = visitRepository.findByPetIdIn(petIds);
        return new Visits(byPetIdIn);
    }


    //This method will delete the visits based on the id
    @DeleteMapping("visits")
    public void deleteVisit(@RequestParam("id") int visitId){
        visitRepository.findById(visitId).ifPresent(e -> visitRepository.delete(e));
    }


    //This class define the object that will be returned for the client with multiples visits
    @Value
    static class Visits {
        List<Visit> items;
    }
}
