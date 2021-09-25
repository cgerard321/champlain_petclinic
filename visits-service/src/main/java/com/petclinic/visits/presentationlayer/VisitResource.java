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

    @PostMapping("owners/*/pets/{petId}/visits")
    @ResponseStatus(HttpStatus.CREATED)
    public Visit create(
            @Valid @RequestBody Visit visit,
            @PathVariable("petId") int petId) {

        visit.setPetId(petId);
        log.info("Saving visit {}", visit);
        return visitRepository.save(visit);
    }


    @GetMapping("owners/*/pets/{petId}/visits")
    public List<Visit> visits(@PathVariable("petId") int petId) {
        return visitRepository.findByPetId(petId);
    }

    @GetMapping("pets/visits")
    public Visits visitsMultiGet(@RequestParam("petId") List<Integer> petIds) {
        final List<Visit> byPetIdIn = visitRepository.findByPetIdIn(petIds);
        return new Visits(byPetIdIn);
    }


    @Value
    static class Visits {
        List<Visit> items;
    }
}
