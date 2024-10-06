package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.Role;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.presentationlayer.BFFApiGatewayController;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/gateway/visits")
@Validated
@Slf4j
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class VisitController {

    private final VisitsServiceClient visitsServiceClient;

    private final BFFApiGatewayController bffApiGatewayController;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<VisitResponseDTO>> getAllVisits() {
        return ResponseEntity.ok().body(visitsServiceClient.getAllVisits());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> addVisit(@RequestBody Mono<VisitRequestDTO> visitResponseDTO) {
        return visitsServiceClient.addVisit(visitResponseDTO)
                .map(v -> ResponseEntity.status(HttpStatus.CREATED).body(v))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
        }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/reviews")
    public ResponseEntity<Flux<ReviewResponseDTO>> getAllReviews(){
        return ResponseEntity.ok().body(visitsServiceClient.getAllReviews());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/reviews", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> PostReview(@RequestBody Mono<ReviewRequestDTO> reviewRequestDTOMono){
        return visitsServiceClient.createReview(reviewRequestDTOMono)
                .map(c->ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @IsUserSpecific(idToMatch = {"reviewId"})
    @PutMapping(value = "/reviews/{reviewId}")
    public Mono<ResponseEntity<ReviewResponseDTO>> updateReview(
            @PathVariable String reviewId,
            @RequestBody Mono<ReviewRequestDTO> reviewRequestDTO) {

        return Mono.just(reviewId)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided review ID is invalid: " + reviewId)))
                .flatMap(id -> visitsServiceClient.updateReview(id, reviewRequestDTO)) // Assuming `updateReview` method exists in `visitsServiceClient`
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @IsUserSpecific(idToMatch = {"reviewId"})
    @GetMapping(value = "/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> getReviewByReviewId(
            @PathVariable String reviewId) {

        return Mono.just(reviewId)// Validate the review ID length
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided review ID is invalid: " + reviewId)))
                .flatMap(id -> visitsServiceClient.getReviewByReviewId(id)) // Assuming `getReviewByReviewId` method exists in `visitsServiceClient`
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PutMapping(value = "/{visitId}")
    public Mono<ResponseEntity<VisitResponseDTO>> updateVisitByVisitId(
            @PathVariable String visitId,
            @RequestBody Mono<VisitRequestDTO> visitRequestDTO) {
        return visitRequestDTO
                .flatMap(request -> visitsServiceClient.updateVisitByVisitId(visitId, Mono.just(request))
                        .map(updatedVisit -> ResponseEntity.ok(updatedVisit))
                        .defaultIfEmpty(ResponseEntity.notFound().build())); // Return 404 if not found
    }
    @IsUserSpecific(idToMatch = {"visitId"})
    @DeleteMapping(value = "/completed/{visitId}")
    public Mono<ResponseEntity<Void>> deleteCompletedVisitByVisitId(@PathVariable String visitId) {
        return visitsServiceClient.deleteCompletedVisitByVisitId(visitId)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    //customer visits
    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping(value = "/owners/{ownerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsByOwnerId(final @PathVariable String ownerId) {
        return bffApiGatewayController.getVisitsByOwnerId(ownerId);
    }

    //Emergency

    @GetMapping(value = "/emergency")
    public Flux<EmergencyResponseDTO> getAllEmergency(){
        return visitsServiceClient.getAllEmergency();
    }

    @GetMapping(value="/emergency/{emergencyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<EmergencyResponseDTO>> getEmergencyByEmergencyId(@PathVariable String emergencyId){
        return Mono.just(emergencyId)
                //.filter(id -> id.length() == 36)
                //.switchIfEmpty(Mono.error(new InvalidInputException("the provided emergency id is invalid: " + emergencyId)))
                .flatMap(visitsServiceClient::getEmergencyByEmergencyId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PostMapping(value = "/emergency")
    public Mono<ResponseEntity<EmergencyResponseDTO>> PostEmergency(@RequestBody Mono<EmergencyRequestDTO> emergencyRequestDTOMono){
        return visitsServiceClient.createEmergency(emergencyRequestDTOMono)
                .map(c->ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping(value="/emergency/{emergencyId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<EmergencyResponseDTO>> UpdateEmergency(@RequestBody Mono<EmergencyRequestDTO> emergencyRequestDTOMono, @PathVariable String emergencyId){
        return Mono.just(emergencyId)
              //  .filter(id -> id.length() == 36)
               // .switchIfEmpty(Mono.error(new InvalidInputException("the provided emergency id is invalid: " + emergencyId)))
                .flatMap(id-> visitsServiceClient.updateEmergency(emergencyId,emergencyRequestDTOMono))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @DeleteMapping(value="/emergency/{emergencyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<EmergencyResponseDTO>> DeteleEmergency(@PathVariable String emergencyId) {
        return Mono.just(emergencyId)
              //  .filter(id -> id.length() == 36)
             //   .switchIfEmpty(Mono.error(new InvalidInputException("the provided emergency id is invalid: " + emergencyId)))
                .flatMap(visitsServiceClient::deleteEmergency)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = Roles.ADMIN)
    @IsUserSpecific(idToMatch = {"visitId"})
    @PatchMapping(value = "/{visitId}/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> updateVisitStatus(
            @PathVariable String visitId, @PathVariable String status) {
        return visitsServiceClient.patchVisitStatus(visitId, status) // Forward to the client
                .map(visitResponseDTO -> new ResponseEntity<>(visitResponseDTO, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Handle empty responses
    }

    @DeleteMapping(value="/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> deleteReview(@PathVariable String reviewId) {
        return Mono.just(reviewId)
                .flatMap(visitsServiceClient::deleteReview)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
