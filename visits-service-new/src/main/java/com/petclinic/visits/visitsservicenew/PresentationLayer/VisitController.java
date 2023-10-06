package com.petclinic.visits.visitsservicenew.PresentationLayer;


import com.petclinic.visits.visitsservicenew.BusinessLayer.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping(value="", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getAllVisits(){
        return visitService.getAllVisits();
    }

    @GetMapping(value="vets/{practitionerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitByVetId(@PathVariable final String practitionerId) {
        return visitService.getVisitsForPractitioner(practitionerId);
    }
    @GetMapping(value="/pets/{petId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsForPet(@PathVariable String petId){
        return visitService.getVisitsForPet(petId);
    }

    @GetMapping(value = "/status/{status}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsForStatus(@PathVariable String status){
        return visitService.getVisitsForStatus(status);
    }

    @GetMapping("/{visitId}")
    public Mono<ResponseEntity<VisitResponseDTO>> getVisitByVisitId(@PathVariable String visitId){
        return visitService.getVisitByVisitId(visitId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /*
    @GetMapping(value="practitioner/{practitionerId}/{month}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsByPractitionerIdAndMonth(@PathVariable int practitionerId, @PathVariable int month){
        return visitService.getVisitsByPractitionerIdAndMonth(practitionerId, month);
    }
     */

    @PostMapping("")
    public Mono<VisitResponseDTO> addVisit(@RequestBody Mono<VisitRequestDTO> visitRequestDTOMono){
        return visitService.addVisit(visitRequestDTOMono);
    }

    @PutMapping(value = "/{visitId}", consumes = "application/json", produces = "application/json")
    public Mono<VisitResponseDTO> updateVisitByVisitId(@PathVariable String visitId, @RequestBody Mono<VisitRequestDTO> visitRequestDTOMono){
        return visitService.updateVisit(visitId, visitRequestDTOMono);
    }

    @PutMapping(value = "/{visitId}/status/{status}", produces = "application/json")
    public Mono<VisitResponseDTO> updateStatusForVisitByVisitId(@PathVariable String visitId, @PathVariable String status){
        return visitService.updateStatusForVisitByVisitId(visitId, status);
    }

    @DeleteMapping("/{visitId}")
    public Mono<ResponseEntity<Void>> deleteVisit(@PathVariable String visitId) {
        return visitService.deleteVisit(visitId)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
    }

    @DeleteMapping("/cancelled")
    public Mono<ResponseEntity<Void>> deleteAllCancelLedVisits(){
        return visitService.deleteAllCancelledVisits()
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
    }


//    @GetMapping("/pets/{petId}")
//    public Mono<PetResponseDTO> getPetByIdTest(@PathVariable int petId){
//       return visitService.testingGetPetDTO(petId);
//    }
//
//    @GetMapping("/vets/{vetId}")
//    public Mono<VetDTO> getVetByIdTest(@PathVariable String vetId){
//        return visitService.testingGetVetDTO(vetId);
//    }
}