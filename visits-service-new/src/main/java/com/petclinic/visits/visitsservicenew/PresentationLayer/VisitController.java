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

    @PostMapping("owners/*/pets/{petId}/visits")
    public Mono<VisitDTO> addVisit(@RequestBody Mono<VisitDTO> visitIdLessDTOMono){
        return visitService.addVisit(visitIdLessDTOMono);
    }

    @PutMapping(value = "owners/*/pets/{petId}/visits/{visitId}",
            consumes = "application/json",
            produces = "application/json")
    public Mono<ResponseEntity<VisitDTO>> updateVisitByVisitId(@PathVariable String visitId, @RequestBody Mono<VisitDTO> visitDTOMono){
        return visitService.updateVisit(visitId, visitDTOMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{visitId}")
    public Mono<Void> deleteVisit(@PathVariable("visitId") String visitId) {
         return visitService.deleteVisit(visitId);
    }

    @GetMapping("practitioner/visits/{practitionerId}/{practitionerDate}")
    public Flux<ResponseEntity<VisitDTO>> getVisitsByPractitionerIdAndMonth(@PathVariable int practitionerId, @PathVariable Date practitionerDate){

        return visitService.getVisitsByPractitionerIdAndMonth(practitionerId, practitionerDate)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("visits/{petId}")
    public Flux<ResponseEntity<VisitDTO>> getVisitsForPet(int petId){

        return visitService.getVisitsForPet(petId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
