package com.petclinic.visits.presentationlayer;


import com.petclinic.visits.businesslayer.VisitsService;
import com.petclinic.visits.datalayer.Visit;
import io.micrometer.core.annotation.Timed;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
/*
 * This class is a REST Controller that handles all the requests coming from the API Gateway.
 *
 * Contributors:
 *   70963776+cjayneb@users.noreply.github.com
 */
/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Maciej Szarlinski
 */
@RestController
@Slf4j
@Timed("petclinic.visit")
public class VisitResource {

    private final VisitsService visitsService;

    public VisitResource(VisitsService service){
        this.visitsService = service;
    }

    @PostMapping("owners/*/pets/{petId}/visits")
    @ResponseStatus(HttpStatus.CREATED)
    public Visit create(
            @Valid @RequestBody Visit visit,
            @PathVariable("petId") int petId) {

        visit.setPetId(petId);
        Visit savedVisit = visitsService.addVisit(visit);
        log.debug("Saving visit {}", savedVisit);
        return savedVisit;
    }


    @DeleteMapping("visits/{visitId}")
    public void deleteVisit(@PathVariable("visitId") int visitId) {
        visitsService.deleteVisit(visitId);
    }

    //This method will return every visits of people that have multiple pets
    @GetMapping("visits/{petId}")
    public List<Visit> getVisitsForPet(@PathVariable("petId") int petId){
        log.info("Getting visits for pet with petid: {}", petId );
        return visitsService.getVisitsForPet(petId);
    }

    @GetMapping("pets/visits")
    public Visits visitsMultiGet(@RequestParam("petId") List<Integer> petIds) {
        final List<Visit> byPetIdIn = visitsService.getVisitsForPets(petIds);
        return new Visits(byPetIdIn);
    }

    @PutMapping(value = "owners/*/pets/{petId}/visits/{id}",
            consumes = "application/json",
            produces = "application/json")
    public Visit update(@Valid @RequestBody Visit visit, @PathVariable("petId") int petId, @PathVariable("id") int id) {
        visit.setId(id);
        visit.setPetId(petId);
        log.info("Updating visit {}", visit);
        return visitsService.updateVisit(visit);
    }

    @GetMapping("visits/previous/{petId}")
    public List<Visit> getPreviousVisitsForPet(@PathVariable("petId") int petId){
        log.debug("Calling VisitsService:getVisitsForPet:previous:petId={}", petId);
        return visitsService.getVisitsForPet(petId, false);
    }

    @GetMapping("visits/scheduled/{petId}")
    public List<Visit> getScheduledVisitsForPet(@PathVariable("petId") int petId){
        log.debug("Calling VisitsService:getVisitsForPet:scheduled:petId={}", petId);
        return visitsService.getVisitsForPet(petId, true);
    }


    @Value
    static class Visits {
        List<Visit> items;
    }
}
