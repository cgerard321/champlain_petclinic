package com.petclinic.visits.visitsservicenew.PresentationLayer;


import com.petclinic.visits.visitsservicenew.BusinessLayer.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("visits")
@RequiredArgsConstructor
public class VisitController {
    private final VisitService visitService;

    @GetMapping("/{visitId}")
    public Mono<ResponseEntity<VisitResponseDTO>> getVisitByVisitId(@PathVariable String visitId){
        return visitService.getVisitByVisitId(visitId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping(value="practitioner/visits/{practitionerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitByPractitionerId(@PathVariable int practitionerId){
        return visitService.getVisitsForPractitioner(practitionerId);
    }

    @PostMapping("")
    public Mono<VisitResponseDTO> addVisit(@RequestBody Mono<VisitRequestDTO> visitRequestDTOMono){
        return visitService.addVisit(visitRequestDTOMono);
    }

    @PutMapping(value = "visits/{visitId}", consumes = "application/json", produces = "application/json")
    public Mono<VisitResponseDTO> updateVisitByVisitId(@PathVariable String visitId, @RequestBody Mono<VisitRequestDTO> visitRequestDTOMono){
        return visitService.updateVisit(visitId, visitRequestDTOMono);
    }

    @DeleteMapping("/{visitId}")
    public Mono<Void> deleteVisit(@PathVariable("visitId") String visitId) {
        return visitService.deleteVisit(visitId);
    }

    @GetMapping(value="practitioner/{practitionerId}/{month}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsByPractitionerIdAndMonth(@PathVariable int practitionerId, @PathVariable int month){
        return visitService.getVisitsByPractitionerIdAndMonth(practitionerId, month);
    }

    @GetMapping(value="/pets/{petId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsForPet(@PathVariable int petId){
        return visitService.getVisitsForPet(petId);
    }
}