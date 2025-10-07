//package com.petclinic.bffapigateway.presentationlayer.v2;
//
//import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
//import com.petclinic.bffapigateway.dtos.Auth.Role;
//import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyRequestDTO;
//import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyResponseDTO;
//import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
//import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
//import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewRequestDTO;
//import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
//import com.petclinic.bffapigateway.exceptions.InvalidInputException;
//import com.petclinic.bffapigateway.presentationlayer.BFFApiGatewayController;
//import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
//import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
//import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.io.InputStreamResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.io.ByteArrayInputStream;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("api/v2/gateway/visits")
//@Validated
//@Slf4j
//public class VisitController {
//
//    private final VisitsServiceClient visitsServiceClient;
//    private final BFFApiGatewayController bffApiGatewayController;
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
//    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public ResponseEntity<Flux<VisitResponseDTO>> getAllVisits(@RequestParam(required = false) String description){
//        return ResponseEntity.ok().body(visitsServiceClient.getAllVisits(description));
//    }
//
//    @SecuredEndpoint(allowedRoles = {Roles.ALL})
//    @GetMapping(value = "/{visitId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<VisitResponseDTO>> getVisitByVisitId(@PathVariable String visitId) {
//        return visitsServiceClient.getVisitByVisitId(visitId)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
//
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.OWNER})
//    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<VisitResponseDTO>> addVisit(@RequestBody Mono<VisitRequestDTO> visitRequestDTO) {
//        return visitsServiceClient.addVisit(visitRequestDTO)
//                .map(v -> ResponseEntity.status(HttpStatus.CREATED).body(v))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//        }
//
//
//    @SecuredEndpoint(allowedRoles = {Roles.ALL})
//    @GetMapping(value = "/reviews")
//    public ResponseEntity<Flux<ReviewResponseDTO>> getAllReviews(){
//        return ResponseEntity.ok().body(visitsServiceClient.getAllReviews());
//    }
//
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @PostMapping(value = "/reviews", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<ReviewResponseDTO>> PostReview(@RequestBody Mono<ReviewRequestDTO> reviewRequestDTOMono){
//        return visitsServiceClient.createReview(reviewRequestDTOMono)
//                .map(c->ResponseEntity.status(HttpStatus.CREATED).body(c))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @IsUserSpecific(idToMatch = {"reviewId"})
//    @PutMapping(value = "/reviews/{reviewId}")
//    public Mono<ResponseEntity<ReviewResponseDTO>> updateReview(
//            @PathVariable String reviewId,
//            @RequestBody Mono<ReviewRequestDTO> reviewRequestDTO) {
//
//        return Mono.just(reviewId)
//                .switchIfEmpty(Mono.error(new InvalidInputException("Provided review ID is invalid: " + reviewId)))
//                .flatMap(id -> visitsServiceClient.updateReview(id, reviewRequestDTO)) // Assuming `updateReview` method exists in `visitsServiceClient`
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @IsUserSpecific(idToMatch = {"reviewId"})
//    @GetMapping(value = "/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<ReviewResponseDTO>> getReviewByReviewId(
//            @PathVariable String reviewId) {
//
//        return Mono.just(reviewId)// Validate the review ID length
//                .switchIfEmpty(Mono.error(new InvalidInputException("Provided review ID is invalid: " + reviewId)))
//                .flatMap(id -> visitsServiceClient.getReviewByReviewId(id)) // Assuming `getReviewByReviewId` method exists in `visitsServiceClient`
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @PutMapping(value = "/{visitId}")
//    public Mono<ResponseEntity<VisitResponseDTO>> updateVisitByVisitId(
//            @PathVariable String visitId,
//            @RequestBody Mono<VisitRequestDTO> visitRequestDTO) {
//        return visitRequestDTO
//                .flatMap(request -> visitsServiceClient.updateVisitByVisitId(visitId, Mono.just(request))
//                        .map(updatedVisit -> ResponseEntity.ok(updatedVisit))
//                        .defaultIfEmpty(ResponseEntity.notFound().build())); // Return 404 if not found
//    }
//
//    //customer visits
//    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
//    @GetMapping(value = "/owners/{ownerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<VisitResponseDTO> getVisitsByOwnerId(final @PathVariable String ownerId) {
//        return bffApiGatewayController.getVisitsByOwnerId(ownerId);
//    }
//
//    //Emergency
//
//    @GetMapping(value = "/emergency")
//    public Flux<EmergencyResponseDTO> getAllEmergency(){
//        return visitsServiceClient.getAllEmergency();
//    }
//
//    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
//    @GetMapping(value = "/emergency/owners/{ownerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<EmergencyResponseDTO> getEmergencyVisitsByOwnerId(final @PathVariable String ownerId) {
//        return bffApiGatewayController.getEmergencyVisitsByOwnerId(ownerId);
//    }
//
//    @PostMapping(value = "/emergency")
//    public Mono<ResponseEntity<EmergencyResponseDTO>> PostEmergency(@RequestBody Mono<EmergencyRequestDTO> emergencyRequestDTOMono){
//        return visitsServiceClient.createEmergency(emergencyRequestDTOMono)
//                .map(c->ResponseEntity.status(HttpStatus.CREATED).body(c))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
//    @GetMapping(value="/emergency/{visitEmergencyId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<EmergencyResponseDTO>> getEmergencyByEmergencyId(@PathVariable String visitEmergencyId){
//        return Mono.just(visitEmergencyId)
//                //.filter(id -> id.length() == 36)
//                //.switchIfEmpty(Mono.error(new InvalidInputException("the provided emergency id is invalid: " + emergencyId)))
//                .flatMap(visitsServiceClient::getEmergencyByEmergencyId)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
//    /*@GetMapping(value="/emergency/{emergencyId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<EmergencyResponseDTO>> getEmergencyByEmergencyId(@PathVariable String emergencyId){
//        return Mono.just(emergencyId)
//                //.filter(id -> id.length() == 36)
//                //.switchIfEmpty(Mono.error(new InvalidInputException("the provided emergency id is invalid: " + emergencyId)))
//                .flatMap(visitsServiceClient::getEmergencyByEmergencyId)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
//    @PostMapping(value = "/emergency")
//    public Mono<ResponseEntity<EmergencyResponseDTO>> PostEmergency(@RequestBody Mono<EmergencyRequestDTO> emergencyRequestDTOMono){
//        return visitsServiceClient.createEmergency(emergencyRequestDTOMono)
//                .map(c->ResponseEntity.status(HttpStatus.CREATED).body(c))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//*/
////    @PutMapping(value="/emergency/{emergencyId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
////    public Mono<ResponseEntity<EmergencyResponseDTO>> UpdateEmergency(@RequestBody Mono<EmergencyRequestDTO> emergencyRequestDTOMono, @PathVariable String emergencyId){
////        return Mono.just(emergencyId)
////                .filter(id -> id.length() == 36)
////                .switchIfEmpty(Mono.error(new InvalidInputException("the provided emergency id is invalid: " + emergencyId)))
////                .flatMap(id-> visitsServiceClient.updateEmergency(emergencyId,emergencyRequestDTOMono))
////                .map(ResponseEntity::ok)
////                .defaultIfEmpty(ResponseEntity.badRequest().build());
////    }
//
//    @PutMapping(value = "/emergency/{visitEmergencyId}",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<EmergencyResponseDTO>> updateEmergencyById(
//            @PathVariable String visitEmergencyId,
//            @RequestBody Mono<EmergencyRequestDTO> emergencyRequestDTOMono) {
//
//        return emergencyRequestDTOMono
//                .flatMap(request -> visitsServiceClient
//                        .updateEmergency(visitEmergencyId, Mono.just(request))
//                        .map(ResponseEntity::ok)
//                        .defaultIfEmpty(ResponseEntity.notFound().build())
//                );
//    }
//
//
//
//
//
//
//
//
//
//
//
//    @DeleteMapping(value="/emergency/{emergencyId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<EmergencyResponseDTO>> DeteleEmergency(@PathVariable String emergencyId) {
//        return Mono.just(emergencyId)
//              //  .filter(id -> id.length() == 36)
//             //   .switchIfEmpty(Mono.error(new InvalidInputException("the provided emergency id is invalid: " + emergencyId)))
//                .flatMap(visitsServiceClient::deleteEmergency)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
//    @SecuredEndpoint(allowedRoles = Roles.ADMIN)
//    @IsUserSpecific(idToMatch = {"visitId"})
//    @PatchMapping(value = "/{visitId}/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<VisitResponseDTO>> updateVisitStatus(
//            @PathVariable String visitId, @PathVariable String status) {
//        return visitsServiceClient.patchVisitStatus(visitId, status) // Forward to the client
//                .map(visitResponseDTO -> new ResponseEntity<>(visitResponseDTO, HttpStatus.OK))
//                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Handle empty responses
//    }
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @IsUserSpecific(idToMatch = {"visitId"})
//    @PutMapping(value = "/completed/{visitId}/archive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<VisitResponseDTO>> archiveCompletedVisit(@PathVariable String visitId, @RequestBody Mono<VisitRequestDTO> visitRequestDTOMono){
//        return visitsServiceClient.archiveCompletedVisit(visitId, visitRequestDTOMono)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @GetMapping(value = "/archived", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<VisitResponseDTO> getArchivedVisits() {
//        return visitsServiceClient.getAllArchivedVisits();
//    }
//
//    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
//    @DeleteMapping(value="/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<ReviewResponseDTO>> deleteReview(@PathVariable String reviewId) {
//        return Mono.just(reviewId)
//                .flatMap(visitsServiceClient::deleteReview)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
//
//    //reviews for user
//    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
//    @GetMapping(value = "/owners/{ownerId}/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Flux<ReviewResponseDTO> getReviewsByOwnerId(final @PathVariable String ownerId) {
//        return visitsServiceClient.getReviewsByOwnerId(ownerId);
//    }
//
//    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
//    @PostMapping(value = "/owners/{ownerId}/reviews", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<ReviewResponseDTO>> addReviewCustomer(@PathVariable String ownerId, @RequestBody Mono<ReviewRequestDTO> reviewRequestDTOMono) {
//        return reviewRequestDTOMono
//                .flatMap(reviewRequestDTO -> visitsServiceClient.addCustomerReview(ownerId, reviewRequestDTO))
//                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
//    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
//    @DeleteMapping(value="/owners/{ownerId}/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<Void>> deleteCustomerReview(@PathVariable String ownerId, @PathVariable String reviewId) {
//        return visitsServiceClient.deleteReview(ownerId, reviewId)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.noContent().build());
//    }
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
//    public Mono<ResponseEntity<InputStreamResource>> exportVisitsToCSV() {
//        return visitsServiceClient.exportVisitsToCSV()
//                .map(csvData -> ResponseEntity.ok()
//                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=visits.csv")
//                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                        .body(csvData));
//    }
//}
