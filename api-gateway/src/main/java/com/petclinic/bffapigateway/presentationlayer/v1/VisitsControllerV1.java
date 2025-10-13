package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.TimeSlotDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/visits")
public class VisitsControllerV1 {

    private final VisitsServiceClient visitsServiceClient;

    private final CustomersServiceClient customersServiceClient;

    /////////////////////////////////////////////
    ///////////Visits Methods///////////////////
    ///////////////////////////////////////////

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<VisitResponseDTO>> getAllVisits(@RequestParam(required = false) String description){
        return ResponseEntity.ok().body(visitsServiceClient.getAllVisits(description));
    }

//    @GetMapping(value ="/{visitId}")
//    public Mono<VisitResponseDTO> getVisitByVisitId(@PathVariable String visitId){
//        return visitsServiceClient.getVisitByVisitId(visitId);
//    }


    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/{visitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> getVisitByVisitId(@PathVariable String visitId) {
        return visitsServiceClient.getVisitByVisitId(visitId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/pets/{petId}/visits", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsForPet(@PathVariable String petId){
        return visitsServiceClient.getVisitsForPet(petId);
    }


    @GetMapping(value = "/status/{status}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsForStatus(@PathVariable String status){
        return visitsServiceClient.getVisitsForStatus(status);
    }

    @GetMapping(value = "/vets/{practitionerId}/visits", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitByPractitionerId(@PathVariable String practitionerId){
        return visitsServiceClient.getVisitByPractitionerId(practitionerId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.OWNER, Roles.VET})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> addVisit(@RequestBody Mono<VisitRequestDTO> visitRequestDTO) {
        return visitsServiceClient.addVisit(visitRequestDTO)
                .map(v -> ResponseEntity.status(HttpStatus.CREATED).body(v))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
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

    @PatchMapping("/{visitId}/status/{status}")
    public Mono<ResponseEntity<VisitResponseDTO>> updateStatusForVisitByVisitId(
            @PathVariable String visitId,
            @PathVariable String status) {

        return visitsServiceClient.updateStatusForVisitByVisitId(visitId, status)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }


    @DeleteMapping("/{visitId}")
    public Mono<ResponseEntity<Void>> deleteVisitsByVisitId(@PathVariable String visitId) {
        return visitsServiceClient.deleteVisitByVisitId(visitId)
                .map(v -> ResponseEntity.noContent().<Void>build()) // deleted
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())); // not found
    }

    @DeleteMapping(value = "/cancelled")
    public Mono<ResponseEntity<Void>> deleteAllCancelledVisits(){
        return visitsServiceClient.deleteAllCancelledVisits()
                .map(v -> ResponseEntity.noContent().<Void>build())
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @IsUserSpecific(idToMatch = {"visitId"})
    @PutMapping(value = "/{visitId}/completed/archive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> archiveCompletedVisit(@PathVariable String visitId, @RequestBody Mono<VisitRequestDTO> visitRequestDTOMono){
        return visitsServiceClient.archiveCompletedVisit(visitId, visitRequestDTOMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/archived", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getArchivedVisits() {
        return visitsServiceClient.getAllArchivedVisits();
    }


    /////////////////////////////////////////////
    ///////////Owner Methods////////////////////
    ///////////////////////////////////////////


//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.OWNER})
//    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
//    @GetMapping(value = "/owners/{ownerId}/visits", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<VisitResponseDTO> getVisitsByOwnerId(@PathVariable String ownerId){
////not ideal since returns complete pet dto
//        return visitsServiceClient.getVisitsForPet(ownerId).flatMap(petResponseDTO -> visitsServiceClient.getVisitsForPet(petResponseDTO.getPetId()));
//    }

    @GetMapping(value = "/owners/{ownerId}/visits", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsByOwnerId(@PathVariable String ownerId) {
        return customersServiceClient.getPetsByOwnerId(ownerId)
                .flatMap(pet -> visitsServiceClient.getVisitsForPet(pet.getPetId()));
    }



    /////////////////////////////////////////////
    ///////////Emergencies Methods//////////////
    ///////////////////////////////////////////

    @GetMapping(value = "/emergencies")
    public Flux<EmergencyResponseDTO> getAllEmergency(){
        return visitsServiceClient.getAllEmergency();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.OWNER})
    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/owners/{ownerId}/emergencies", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmergencyResponseDTO> getEmergencyVisitsByOwnerId(@PathVariable String ownerId){
//not ideal since returns complete pet dto
        return customersServiceClient.getPetsByOwnerId(ownerId).flatMap(petResponseDTO -> visitsServiceClient.getEmergencyVisitForPet(petResponseDTO.getPetId()));
    }

    @GetMapping(value = "/pets/{petId}/emergencies", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmergencyResponseDTO> getEmergencyVisitsForPet(@PathVariable String petId){
        return visitsServiceClient.getEmergencyVisitForPet(petId);
    }

    @GetMapping(value="/emergencies/{visitEmergencyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<EmergencyResponseDTO>> getEmergencyByEmergencyId(@PathVariable String visitEmergencyId){
        return Mono.just(visitEmergencyId)
                //.filter(id -> id.length() == 36)
                //.switchIfEmpty(Mono.error(new InvalidInputException("the provided emergency id is invalid: " + emergencyId)))
                .flatMap(visitsServiceClient::getEmergencyByEmergencyId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/emergencies")
    public Mono<ResponseEntity<EmergencyResponseDTO>> PostEmergency(@RequestBody Mono<EmergencyRequestDTO> emergencyRequestDTOMono){
        return visitsServiceClient.createEmergency(emergencyRequestDTOMono)
                .map(c->ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping(value = "/emergencies/{visitEmergencyId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<EmergencyResponseDTO>> updateEmergencyById(
            @PathVariable String visitEmergencyId,
            @RequestBody Mono<EmergencyRequestDTO> emergencyRequestDTOMono) {

        return emergencyRequestDTOMono
                .flatMap(request -> visitsServiceClient
                        .updateEmergency(visitEmergencyId, Mono.just(request))
                        .map(ResponseEntity::ok)
                        .defaultIfEmpty(ResponseEntity.notFound().build())
                );
    }

    @DeleteMapping(value="/emergencies/{emergencyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<EmergencyResponseDTO>> deleteEmergency(@PathVariable String emergencyId) {
        return Mono.just(emergencyId)
                //  .filter(id -> id.length() == 36)
                //   .switchIfEmpty(Mono.error(new InvalidInputException("the provided emergency id is invalid: " + emergencyId)))
                .flatMap(visitsServiceClient::deleteEmergency)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }





    /////////////////////////////////////////////
    ///////////Reviews Methods//////////////////
    ///////////////////////////////////////////

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/reviews")
    public ResponseEntity<Flux<ReviewResponseDTO>> getAllReviews(){
        return ResponseEntity.ok().body(visitsServiceClient.getAllReviews());
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

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping(value = "/owners/{ownerId}/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ReviewResponseDTO> getReviewsByOwnerId(final @PathVariable String ownerId) {
        return visitsServiceClient.getReviewsByOwnerId(ownerId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/reviews", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> PostReview(@RequestBody Mono<ReviewRequestDTO> reviewRequestDTOMono){
        return visitsServiceClient.createReview(reviewRequestDTOMono)
                .map(c->ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
    @PostMapping(value = "/owners/{ownerId}/reviews", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> addReviewCustomer(@PathVariable String ownerId, @RequestBody Mono<ReviewRequestDTO> reviewRequestDTOMono) {
        return reviewRequestDTOMono
                .flatMap(reviewRequestDTO -> visitsServiceClient.addCustomerReview(ownerId, reviewRequestDTO))
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
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
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping(value="/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> deleteReview(@PathVariable String reviewId) {
        return Mono.just(reviewId)
                .flatMap(visitsServiceClient::deleteReview)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
    @DeleteMapping(value="/owners/{ownerId}/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> deleteCustomerReview(@PathVariable String ownerId, @PathVariable String reviewId) {
        return visitsServiceClient.deleteReview(ownerId, reviewId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<InputStreamResource>> exportVisitsToCSV() {
        return visitsServiceClient.exportVisitsToCSV()
                .map(csvData -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=visits.csv")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(csvData));
    }


    /////////////////////////////////////////////
///////////Availability Methods//////////////////
///////////////////////////////////////////

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.OWNER, Roles.VET})
    @GetMapping(value = "/availability/vets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Flux<VetResponseDTO>> getAllVetsForAvailability() {
        return ResponseEntity.ok().body(visitsServiceClient.getAllVetsForAvailability());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.OWNER, Roles.VET})
    @GetMapping(value = "/availability/vets/{vetId}/slots", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Flux<TimeSlotDTO>> getAvailableTimeSlots(
            @PathVariable String vetId,
            @RequestParam String date) {
        return ResponseEntity.ok().body(visitsServiceClient.getAvailableTimeSlots(vetId, date));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.OWNER, Roles.VET})
    @GetMapping(value = "/availability/vets/{vetId}/dates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Flux<String>> getAvailableDates(
            @PathVariable String vetId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok().body(visitsServiceClient.getAvailableDates(vetId, startDate, endDate));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.OWNER, Roles.VET})
    @GetMapping(value = "/availability/vets/{vetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> getVeterinarianAvailability(@PathVariable String vetId) {
        return visitsServiceClient.getVeterinarianAvailability(vetId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


}
