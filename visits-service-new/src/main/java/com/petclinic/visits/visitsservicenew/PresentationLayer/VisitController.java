package com.petclinic.visits.visitsservicenew.PresentationLayer;


import com.petclinic.visits.visitsservicenew.BusinessLayer.VisitService;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitIdLessDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("visits")
public class VisitController {

    @Autowired
    VisitService visitService;

    @GetMapping("/{visitId}")
    public Mono<ResponseEntity<VisitDTO>> getVisitByVisitId(@PathVariable String visitId){
        return visitService.getVisitByVisitId(visitId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("practitioner/visits/{practitionerId}")
    public Flux<VisitDTO> getVisitByPractitionerId(@PathVariable int practitionerId){
        return visitService.getVisitsForPractitioner(practitionerId);
    }

    @PostMapping()
    public Mono<VisitDTO> addVisit(@RequestBody Mono<VisitDTO> visitDTOMono){
        return visitService.addVisit(visitDTOMono);
    }

    @PutMapping(value = "visits/{visitId}",
            consumes = "application/json",
            produces = "application/json")
    public Mono<VisitDTO> updateVisitByVisitId(@PathVariable String visitId, @RequestBody Mono<VisitDTO> visitDTOMono){
        return visitService.updateVisit(visitId, visitDTOMono);
    }

    @DeleteMapping("/{visitId}")
    public Mono<Void> deleteVisit(@PathVariable("visitId") String visitId) {
         return visitService.deleteVisit(visitId);
    }

    @GetMapping("practitioner/{practitionerId}/{month}")
    public Flux<VisitDTO> getVisitsByPractitionerIdAndMonth(@PathVariable int practitionerId, @PathVariable int month){

        return visitService.getVisitsByPractitionerIdAndMonth(practitionerId, month);
    }

    @GetMapping("/pets/{petId}")
    public Flux<VisitDTO> getVisitsForPet(@PathVariable int petId){

        return visitService.getVisitsForPet(petId);
    }
}
