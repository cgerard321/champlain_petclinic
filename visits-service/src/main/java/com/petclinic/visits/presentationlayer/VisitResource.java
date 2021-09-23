package com.petclinic.visits.presentationlayer;

import com.petclinic.visits.businesslayer.VisitsService;
import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final VisitsService visitsService;

    public VisitResource(VisitsService service){
        this.visitsService = service;
    }

    //private final VisitRepository visitRepository;

//    @PostMapping("owners/*/pets/{petId}/visits")
//    @ResponseStatus(HttpStatus.CREATED)
//    public Visit create(
//            @Valid @RequestBody Visit visit,
//            @PathVariable("petId") int petId) {
//
//        visit.setPetId(petId);
//        log.info("Saving visit {}", visit);
//        return visitsService.addVisit(visit);
//    }
//
//    @GetMapping("visits/{petId}")
//    public List<Visit> getVisitsForPet(@PathVariable("petId") int petId){
//        log.info("Getting visits for pet with petid: {}", petId );
//        return visitsService.getVisitsForPet(petId);
//    }
//
//    @GetMapping("owners/*/pets/{petId}/visits")
//    public List<Visit> visits(@PathVariable("petId") int petId) {
//        return visitsService.getVisitsForPet(petId);
//    }

    @DeleteMapping("visits/{visitId}")
    public void deleteVisit(@PathVariable("visitId") int visitId){
        visitsService.deleteVisit(visitId);
    }

//    @GetMapping("pets/visits")
//    public Visits visitsMultiGet(@RequestParam("petId") List<Integer> petIds) {
//        final List<Visit> byPetIdIn = visitsService.getVisitsForPets(petIds);
//        return new Visits(byPetIdIn);
//    }

    @Value
    static class Visits {
        List<Visit> items;
    }
}
