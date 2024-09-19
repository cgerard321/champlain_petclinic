package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.Role;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
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

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<VisitResponseDTO>> getAllVisits() {
        return ResponseEntity.ok().body(visitsServiceClient.getAllVisits());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> addVisit(@RequestBody Mono<VisitRequestDTO> visitResponseDTO) {
        return visitsServiceClient.addVisit(visitResponseDTO).map(ResponseEntity::ok);
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

}
