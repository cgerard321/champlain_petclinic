package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.businesslayer.ReviewEligibilityService;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.CancellationRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.Prescriptions.PrescriptionResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.RescheduleRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.TimeSlotDTO;
import com.petclinic.bffapigateway.dtos.Visits.VetNoteRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
import com.petclinic.bffapigateway.exceptions.ExistingPrescriptionNotFoundException;
import com.petclinic.bffapigateway.exceptions.ForbiddenAccessException;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.petclinic.bffapigateway.exceptions.NoTokenFoundException;

import java.util.Map;
import java.util.stream.Stream;

@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/visits")
public class VisitsControllerV1 {

    private final VisitsServiceClient visitsServiceClient;

    private final CustomersServiceClient customersServiceClient;

    private final ReviewEligibilityService reviewEligibilityService;


    /////////////////////////////////////////////
    ///////////Visits Methods///////////////////
    ///////////////////////////////////////////

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.RECEPTIONIST})
    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<VisitResponseDTO>> getAllVisits(@RequestParam(required = false) String description){
        return ResponseEntity.ok().body(visitsServiceClient.getAllVisits(description));
    }

    @SecuredEndpoint(allowedRoles = {Roles.VET})
    @GetMapping(value = "/search", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> searchVisits(@RequestParam String query, ServerWebExchange exchange) {
        TokenResponseDTO user = exchange.getAttribute("tokenValues");
        if (user == null) {
            throw new IllegalStateException("Authenticated user is missing from the validated token.");
        }
        String search = query.trim().toLowerCase();

        return visitsServiceClient.getVisitByPractitionerId(user.getUserId())
                .filter(visit -> matchesSearch(visit, search));
    }

    private boolean matchesSearch(VisitResponseDTO visit, String search) {
        return Stream.of(
                        visit.getVisitId(),
                        visit.getPetName(),
                        visit.getDescription())
                .anyMatch(value -> value != null && value.toLowerCase().contains(search));
    }

//    @GetMapping(value ="/{visitId}")
//    public Mono<VisitResponseDTO> getVisitByVisitId(@PathVariable String visitId){
//        return visitsServiceClient.getVisitByVisitId(visitId);
//    }


    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/{visitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> getVisitByVisitId(
            @PathVariable String visitId,
            @RequestParam(required = false, defaultValue = "false") boolean includePrescription,
            ServerWebExchange exchange) {

        TokenResponseDTO user = exchange.getAttribute("tokenValues");
        return visitsServiceClient.getVisitByVisitId(visitId, includePrescription)
                .flatMap(visit -> hideVetNoteFromOtherOwners(visit, user))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Only the owner of the visit's pet can read the vet note
    private Mono<VisitResponseDTO> hideVetNoteFromOtherOwners(VisitResponseDTO visit, TokenResponseDTO user) {
        boolean isOwner = user != null && user.getRoles() != null
                && user.getRoles().stream().anyMatch(role -> role.contains(Roles.CUST.toString()));
        if (visit.getVetNote() == null || !isOwner) {
            return Mono.just(visit);
        }

        return customersServiceClient.getPetByPetId(visit.getPetId(), false)
                .filter(pet -> user.getUserId().equals(pet.getCustomerId()))
                .map(pet -> visit)
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    visit.setVetNote(null);
                    return visit;
                }));
    }

    @GetMapping(value = "/pets/{petId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
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

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.CUST, Roles.VET})
    @PostMapping(value =
            "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> addVisit(@RequestBody Mono<VisitRequestDTO> visitRequestDTO,
                                                           @CookieValue("Bearer") String jwtToken) {
        return visitRequestDTO
                .flatMap(request -> customersServiceClient.getPetByPetId(request.getPetId(), false)
                        .map(pet -> {
                            request.setOwnerId(pet.getCustomerId());
                            request.setJwtToken(jwtToken);
                            return request;
                        }))
                .flatMap(request -> visitsServiceClient.addVisit(Mono.just(request)))
                .map(v -> ResponseEntity.status(HttpStatus.CREATED).body(v))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.RECEPTIONIST})
    @PutMapping(value = "/{visitId}")
    public Mono<ResponseEntity<VisitResponseDTO>> updateVisitByVisitId(
            @PathVariable String visitId,
            @RequestBody Mono<VisitRequestDTO> visitRequestDTO) {
        return visitRequestDTO
                .flatMap(request -> visitsServiceClient.updateVisitByVisitId(visitId, Mono.just(request))
                        .map(updatedVisit -> ResponseEntity.ok(updatedVisit))
                        .defaultIfEmpty(ResponseEntity.notFound().build())); // Return 404 if not found
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.CUST})
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
    public Mono<ResponseEntity<Void>> deleteAllCancelledVisits() {
        return visitsServiceClient.deleteAllCancelledVisits()
                .thenReturn(ResponseEntity.noContent().build());
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


//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.CUST})
//    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
//    @GetMapping(value = "/owners/{ownerId}/visits", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<VisitResponseDTO> getVisitsByOwnerId(@PathVariable String ownerId){
////not ideal since returns complete pet dto
//        return visitsServiceClient.getVisitsForPet(ownerId).flatMap(petResponseDTO -> visitsServiceClient.getVisitsForPet(petResponseDTO.getPetId()));
//    }

    @GetMapping(value = "/owners/{ownerId}/visits", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsByOwnerId(@PathVariable String ownerId) {
        return customersServiceClient.getPetsByCustomerId(ownerId)
                .flatMap(pet -> visitsServiceClient.getVisitsForPet(pet.getPetId()));
    }


    /////////////////////////////////////////////
    ///////////Reviews Methods//////////////////
    ///////////////////////////////////////////

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/reviews")
    public ResponseEntity<Flux<ReviewResponseDTO>> getAllReviews(){
        return ResponseEntity.ok().body(visitsServiceClient.getAllReviews());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> getReviewByReviewId(
            @PathVariable String reviewId) {

        return Mono.just(reviewId)// Validate the review ID length
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided review ID is invalid: " + reviewId)))
                .flatMap(id -> visitsServiceClient.getReviewByReviewId(id)) // Assuming `getReviewByReviewId` method exists in `visitsServiceClient`
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.CUST})
    @GetMapping(
            value = "/reviews/eligibility",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<Map<String, Boolean>> getReviewEligibility(
            ServerWebExchange exchange) {

        String customerId = authenticatedCustomerId(exchange);

        return reviewEligibilityService
                .hasCompletedAppointment(customerId)
                .map(eligible -> Map.of("eligible", eligible));
    }





    @SecuredEndpoint(allowedRoles = {Roles.CUST})
    @PostMapping(
            value = "/reviews",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<ReviewResponseDTO>> PostReview(
            @RequestBody Mono<ReviewRequestDTO> reviewRequestDTOMono,
            ServerWebExchange exchange) {

        String customerId = authenticatedCustomerId(exchange);

        return reviewRequestDTOMono
                .flatMap(review ->
                        reviewEligibilityService
                                .hasCompletedAppointment(customerId)
                                .flatMap(eligible -> {
                                    if (!eligible) {
                                        return Mono.error(new ForbiddenAccessException("You must have at least one completed appointment before you can leave a review."));}
                                    review.setOwnerId(customerId);
                                    return visitsServiceClient
                                            .createReview(Mono.just(review));}))
                .map(created ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(created))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }



    private String authenticatedCustomerId(ServerWebExchange exchange) {
        TokenResponseDTO user = exchange.getAttribute("tokenValues");

        if (user == null || user.getUserId() == null || user.getUserId().isBlank()) {
            throw new NoTokenFoundException("Authenticated customer not found.");
        }

        return user.getUserId();
    }



    @SecuredEndpoint(allowedRoles = {Roles.CUST, Roles.ADMIN})
    @PutMapping(value = "/reviews/{reviewId}")
    public Mono<ResponseEntity<ReviewResponseDTO>> updateReview(
            @PathVariable String reviewId,
            @RequestBody Mono<ReviewRequestDTO> reviewRequestDTO,
            ServerWebExchange exchange) {

        TokenResponseDTO user = exchange.getAttribute("tokenValues");

        return visitsServiceClient.getReviewByReviewId(reviewId)
                .flatMap(existing -> {
                    if (!isAdmin(user) && !isReviewAuthor(existing, user)) {
                        return Mono.error(new ForbiddenAccessException("You can only edit your own reviews."));
                    }
                    // The author never changes on an edit, whatever the body says
                    return visitsServiceClient.updateReview(reviewId, reviewRequestDTO
                            .doOnNext(review -> {
                                review.setOwnerId(existing.getOwnerId());
                                review.setReviewerId(existing.getReviewerId());
                            }));
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private boolean isAdmin(TokenResponseDTO user) {
        return user != null && user.getRoles() != null
                && user.getRoles().stream().anyMatch(role -> role.contains(Roles.ADMIN.toString()));
    }

    // Seeded reviews only have a reviewerId, reviews posted through the gateway also have the ownerId
    private boolean isReviewAuthor(ReviewResponseDTO review, TokenResponseDTO user) {
        return user != null && user.getUserId() != null
                && (user.getUserId().equals(review.getReviewerId()) || user.getUserId().equals(review.getOwnerId()));
    }

    @SecuredEndpoint(allowedRoles = {Roles.CUST, Roles.ADMIN})
    @DeleteMapping(value="/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> deleteReview(@PathVariable String reviewId) {
        return Mono.just(reviewId)
                .flatMap(visitsServiceClient::deleteReview)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.CUST})
    @DeleteMapping(value="/owners/{ownerId}/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> deleteCustomerReview(
            @PathVariable String ownerId,
            @PathVariable String reviewId) {
        return visitsServiceClient.deleteReview(reviewId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
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

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.CUST, Roles.VET})
    @GetMapping(value = "/availability/vets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Flux<VetResponseDTO>> getAllVetsForAvailability() {
        return ResponseEntity.ok().body(visitsServiceClient.getAllVetsForAvailability());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.CUST, Roles.VET})
    @GetMapping(value = "/availability/vets/{vetId}/slots", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Flux<TimeSlotDTO>> getAvailableTimeSlots(
            @PathVariable String vetId,
            @RequestParam String date) {
        return ResponseEntity.ok().body(visitsServiceClient.getAvailableTimeSlots(vetId, date));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.CUST, Roles.VET})
    @GetMapping(value = "/availability/vets/{vetId}/dates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Flux<String>> getAvailableDates(
            @PathVariable String vetId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok().body(visitsServiceClient.getAvailableDates(vetId, startDate, endDate));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.CUST, Roles.VET})
    @GetMapping(value = "/availability/vets/{vetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> getVeterinarianAvailability(@PathVariable String vetId) {
        return visitsServiceClient.getVeterinarianAvailability(vetId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    /////////////////////////////////////////////
    /////////// Prescription Methods ////////////
    /////////////////////////////////////////////

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PostMapping(
            value = "/{visitId}/prescription",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PrescriptionResponseDTO>> createPrescription(
            @PathVariable String visitId,
            @RequestBody Mono<PrescriptionResponseDTO> prescriptionRequest) {
        return visitsServiceClient.createPrescription(visitId, prescriptionRequest)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(ExistingPrescriptionNotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.CUST})
    @GetMapping(value = "/{visitId}/prescription/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadPrescriptionPdf(
            @PathVariable String visitId) {

        return visitsServiceClient.downloadPrescriptionPdf(visitId)
                .map(pdfBytes -> new ResponseEntity<>(pdfBytes, HttpStatus.OK))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.CUST, Roles.VET})
    @PatchMapping(value = "/{visitId}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> cancelVisit(@PathVariable String visitId, @RequestBody CancellationRequestDTO cancellationRequestDTO) {

        return visitsServiceClient
                .cancelVisit(visitId, cancellationRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.VET})
    @PatchMapping(value = "/{visitId}/note", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> addVetNote(@PathVariable String visitId, @RequestBody VetNoteRequestDTO vetNoteRequestDTO) {
        return visitsServiceClient
                .addVetNote(visitId, vetNoteRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.VET})
    @PostMapping("/weekly-email")
    public Mono<ResponseEntity<String>> sendMyWeeklyScheduleEmail(
            ServerWebExchange exchange) {

        TokenResponseDTO user = exchange.getAttribute("tokenValues");

        if (user == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return visitsServiceClient
                .sendWeeklyScheduleEmail(user.getUserId())
                .map(ResponseEntity::ok);
    }
    @SecuredEndpoint(allowedRoles = {Roles.CUST})
    @PatchMapping(value = "/{visitId}/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> confirmFollowUpVisit(@PathVariable String visitId, ServerWebExchange exchange) {
        TokenResponseDTO user = exchange.getAttribute("tokenValues");
        return findVisitOfCustomer(visitId, user)
                .flatMap(visit -> visitsServiceClient.confirmFollowUpVisit(visitId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.CUST})
    @PatchMapping(value = "/{visitId}/reschedule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> rescheduleFollowUpVisit(@PathVariable String visitId, @RequestBody RescheduleRequestDTO rescheduleRequestDTO, ServerWebExchange exchange) {
        TokenResponseDTO user = exchange.getAttribute("tokenValues");
        return findVisitOfCustomer(visitId, user)
                .flatMap(visit -> visitsServiceClient.rescheduleFollowUpVisit(visitId, rescheduleRequestDTO))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Customers can only change visits for their own pets
    private Mono<VisitResponseDTO> findVisitOfCustomer(String visitId, TokenResponseDTO user) {
        return visitsServiceClient.getVisitByVisitId(visitId, false)
                .flatMap(visit -> customersServiceClient.getPetByPetId(visit.getPetId(), false)
                        .filter(pet -> user != null && user.getUserId().equals(pet.getCustomerId()))
                        .switchIfEmpty(Mono.error(new ForbiddenAccessException("You can only change visits for your own pets")))
                        .thenReturn(visit));
    }


}
