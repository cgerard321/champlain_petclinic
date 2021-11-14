package com.petclinic.visits.presentationlayer;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.petclinic.visits.businesslayer.VisitsService;
import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitDTO;
import com.petclinic.visits.datalayer.VisitIdLessDTO;
import io.micrometer.core.annotation.Timed;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
    public VisitDTO create(
            @Valid @RequestBody VisitIdLessDTO visit,
            @PathVariable("petId") int petId) {

        visit.setPetId(petId);
        log.debug("Calling VisitService:addVisit for pet with petId: {}", petId);
        VisitDTO savedVisit = visitsService.addVisit(visit);
        return savedVisit;
    }

    @DeleteMapping("visits/{visitId}")
    public void deleteVisit(@PathVariable("visitId") String visitId) {
        log.info("Deleting visits with visitId: {}", visitId );
        visitsService.deleteVisit(visitId);
    }

    @GetMapping("visits/{petId}")
    public List<VisitDTO> getVisitsForPet(@PathVariable("petId") int petId){
        log.info("Getting visits for pet with petid: {}", petId );
        return visitsService.getVisitsForPet(petId);
    }

    //This method will return one visit based on the visit id
    @GetMapping("visit/{visitId}")
    public VisitDTO getVisitByVisitId(@PathVariable("visitId") String visitId){
        log.info("Getting visit for visit with visitId: {}", visitId);
        return visitsService.getVisitByVisitId(visitId);
    }

    //This method will return every visits of people that have multiple pets
    @GetMapping("pets/visits")
    public Visits visitsMultiGet(@RequestParam("petId") List<Integer> petIds) {
        final List<VisitDTO> byPetIdIn = visitsService.getVisitsForPets(petIds);
        return new Visits(byPetIdIn);
    }

    @PutMapping(value = "owners/*/pets/{petId}/visits/{visitId}",
            consumes = "application/json",
            produces = "application/json")
    public VisitDTO update(@Valid @RequestBody VisitDTO visitDTO, @PathVariable("petId") int petId, @PathVariable("visitId") String visitId) {
        visitDTO.setVisitId(visitId);
        visitDTO.setPetId(petId);
        log.info("Updating visit {}", visitDTO);
        return visitsService.updateVisit(visitDTO);
    }

    @GetMapping("visits/previous/{petId}")
    public List<VisitDTO> getPreviousVisitsForPet(@PathVariable("petId") int petId){
        log.debug("Calling VisitsService:getVisitsForPet:previous:petId={}", petId);
        return visitsService.getVisitsForPet(petId, false);
    }

    @GetMapping("visits/scheduled/{petId}")
    public List<VisitDTO> getScheduledVisitsForPet(@PathVariable("petId") int petId){
        log.debug("Calling VisitsService:getVisitsForPet:scheduled:petId={}", petId);
        return visitsService.getVisitsForPet(petId, true);
    }

    @GetMapping("visits/vets/{practitionerId}")
    public List<VisitDTO> getVisitsForPractitioner(@PathVariable("practitionerId") int practitionerId){
        log.debug("Calling VisitsService:getVisitsForPractitioner:practitionerId={}", practitionerId);
        return visitsService.getVisitsForPractitioner(practitionerId);
    }

    @GetMapping("visits/calendar/{practitionerId}")
    public List<VisitDTO> getVisitsByPractitionerIdAndMonth(@PathVariable("practitionerId") int practitionerId,
                                                         @RequestParam("dates") List<String> dates)
                                                         throws ParseException {

        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(dates.get(0));
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(dates.get(1));

        log.debug("Calling VisitsService:getVisitsByPractitionerIdAndMonth:practitionerId={}:startDate={},endDate={}", practitionerId, startDate, endDate);
        return visitsService.getVisitsByPractitionerIdAndMonth(practitionerId, startDate, endDate);
    }

    @Value
    static class Visits {
        List<VisitDTO> items;
    }
}
