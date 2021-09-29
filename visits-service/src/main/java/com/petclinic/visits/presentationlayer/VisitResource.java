package com.petclinic.visits.presentationlayer;


import com.petclinic.visits.businesslayer.VisitsService;
import com.petclinic.visits.businesslayer.VisitsServiceImpl;
import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
@Slf4j
@Timed("petclinic.visit")
public class VisitResource {

    //private static final Logger LOG = LoggerFactory.getLogger(VisitResource.class);
    //private final VisitRepository visitRepository;


    private final VisitsService visitsService;

    public VisitResource(VisitsService service){
        this.visitsService = service;
    }


    /*

    private final VisitRepository visitRepository;


    //Testing purposes to View all visits
    @GetMapping(value = "/pets/visits/All")
    public List<Visit> showVisitList() {
        return (List<Visit>) visitRepository.findAll();
    }

    @PostMapping("/pets/visits")
    @ResponseStatus(HttpStatus.OK)
    public Visit createVisit(@Valid @RequestBody Visit visit){

        visit.setId(visit.getId());
        visit.setPetId(visit.getPetId());
        visit.setDate(visit.getDate());
        visit.setDescription(visit.getDescription());

        log.info("Saving visit {}", visit);
        return visitRepository.save(visit);
    }
*/

/*  FOR REFERENCE

    @PutMapping("/pets/visits/{petId}")
    @ResponseStatus(HttpStatus.OK)
    public Visit updateVisit(@PathVariable("petId") int petId, @Valid @RequestBody Visit visit){

        visit.setId(petId);
        visit.setPetId(visit.getPetId());
        visit.setDate(visit.getDate());
        visit.setDescription(visit.getDescription());

        log.info("Updating visit {}", visit);
        return visitRepository.save(visit);
    }

    @DeleteMapping (value = "/pets/visits/{petId}")
    @ResponseStatus(HttpStatus.OK)
    public Visit deleteVisit(@PathVariable("petId") int petId, @Valid @RequestBody Visit visit){
        log.info("Deleting visit {}", visit);
        visitRepository.deleteAll(visitRepository.findByPetId(petId));
        return visit;
    }
*/

    @PostMapping("owners/*/pets/{petId}/visits")
    @ResponseStatus(HttpStatus.CREATED)
    public Visit create(
            @Valid @RequestBody Visit visit,
            @PathVariable("petId") int petId) {
        visit.setPetId(petId);
        log.info("Saving visit {}", visit);
        return visitsService.addVisit(visit);
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


    @Value
    static class Visits {
        List<Visit> items;
    }
}
