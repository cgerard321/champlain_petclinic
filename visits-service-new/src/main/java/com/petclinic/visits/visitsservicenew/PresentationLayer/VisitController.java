package com.petclinic.visits.visitsservicenew.PresentationLayer;


import com.petclinic.visits.visitsservicenew.BusinessLayer.Review.ReviewService;
import com.petclinic.visits.visitsservicenew.BusinessLayer.VisitService;
import com.petclinic.visits.visitsservicenew.Exceptions.InvalidInputException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Application Endpoint for Visit
 */
@RestController
@RequestMapping("visits")
@RequiredArgsConstructor
public class VisitController {
    /**
     * We are Accessing the Controller
     *
     */
    private final VisitService visitService;
    private final ReviewService reviewService;

    /**
     * Simple Get all Visits
     * Accessible through localhost:8080/visits
     * @return All visits
     */
    @GetMapping(value="", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getAllVisits(){
        return visitService.getAllVisits();
    }

    /**
     * Get all the visits with the vet ID given
     * localhost:8080/visits/practitioner/{VET_ID}
     * @param practitionerId The Vet ID
     * @return List of visits that have the Given vetID
     */
    @GetMapping(value="practitioner/{practitionerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsByPractitionerId(@PathVariable String practitionerId) {
        return visitService.getVisitsForPractitioner(practitionerId);
    }

    /**
     * Get all the visits with the pet ID given
     * localhost:8080/visits/pets/{petID}
     * @param petId The Pet id to find the visit for
     * @return All the visit with the common pet ID
     */
    @GetMapping(value="/pets/{petId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsForPet(@PathVariable String petId){
        return visitService.getVisitsForPet(petId);
    }

    /**
     * Get all the visits by their status ( EX : DataLayer/Status ( ENUM ) )
     * localhost:8080/visits/status/{Status.toString}
     * @param status The status we are searching for
     * @return All the visits with the status we searched for
     */
    @GetMapping(value = "/status/{status}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsForStatus(@PathVariable String status){
        return visitService.getVisitsForStatus(status);
    }

    /**
     * Get a visit by its ID
     * localhost:8080/visits/{searchedVisitID}
     * @param visitId The ID of the visit we are searching for
     * @return The visit we searched for or a not found exception
     */
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

    /**
     * Add a new visit. ALSO SEND AN EMAIL
     * localhost:8080/visits @POST
     * @param visitRequestDTOMono The Request DTO
     * @return The response model of the visit we created
     */
    @PostMapping("")
    public Mono<VisitResponseDTO> addVisit(@RequestBody Mono<VisitRequestDTO> visitRequestDTOMono){
        return visitService.addVisit(visitRequestDTOMono);
    }

    /**
     * Replace the information of a visit ( Not visit ID )
     * localhost:8080/visits,{existing visit ID} @PUT
     * @param visitId The visit we want to modify
     * @param visitRequestDTOMono The new body of the visit
     * @return The new modified visit
     */
    @PutMapping(value = "/{visitId}", consumes = "application/json", produces = "application/json")
    public Mono<VisitResponseDTO> updateVisitByVisitId(@PathVariable String visitId, @RequestBody Mono<VisitRequestDTO> visitRequestDTOMono){
        return visitService.updateVisit(visitId, visitRequestDTOMono);
    }

    /**
     * Change the status of a visit
     * localhost:8080/visits/{existing visit ID}/status/{new Status} @PUT
     * @param visitId The visit ID of the visit we want to change the status
     * @param status The new status DataLayer/Status.toString
     * @return The modified Visit
     */
    @PutMapping(value = "/{visitId}/status/{status}", produces = "application/json")
    public Mono<VisitResponseDTO> updateStatusForVisitByVisitId(@PathVariable String visitId, @PathVariable String status){
        return visitService.updateStatusForVisitByVisitId(visitId, status);
    }

    /**
     * Delete a visit by its ID
     * localhost:8080/visits/{visitId} @DEL
     * @param visitId The ID of the visit we want to delete
     * @return The body of the visit we just deleted
     */
    @DeleteMapping("/{visitId}")
    public Mono<ResponseEntity<Void>> deleteVisit(@PathVariable String visitId) {
        return visitService.deleteVisit(visitId)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
    }

    /**
     * Delete all visit whose status DataLayer/Status is Cancelled
     * localhost:8080/visits/{visitId} @DEL
     * @return Deleted Visits
     */
    @DeleteMapping("/cancelled")
    public Mono<ResponseEntity<Void>> deleteAllCancelLedVisits(){
        return visitService.deleteAllCancelledVisits()
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
    }


    @GetMapping(value = "/reviews")
    public Flux<ReviewResponseDTO> getAllReviews(){
        return reviewService.GetAllReviews();
    }

    @GetMapping(value="/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> getReviewByReviewId(@PathVariable String reviewId){
        return Mono.just(reviewId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("the provided review id is invalid: " + reviewId)))
                .flatMap(reviewService::GetReviewByReviewId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PostMapping(value = "/reviews")
    public Mono<ResponseEntity<ReviewResponseDTO>> PostReview(@RequestBody Mono<ReviewRequestDTO> reviewRequestDTOMono){
        return reviewService.AddReview(reviewRequestDTOMono)
                .map(c->ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping(value="/reviews/{reviewId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> UpdateReview(@RequestBody Mono<ReviewRequestDTO> reviewRequestDTOMono, @PathVariable String reviewId){
        return Mono.just(reviewId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("the provided review id is invalid: " + reviewId)))
                .flatMap(id-> reviewService.UpdateReview(reviewRequestDTOMono,reviewId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @DeleteMapping(value="/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> DeteleReview(@PathVariable String reviewId) {
        return Mono.just(reviewId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("the provided review id is invalid: " + reviewId)))
                .flatMap(reviewService::DeleteReview)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
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