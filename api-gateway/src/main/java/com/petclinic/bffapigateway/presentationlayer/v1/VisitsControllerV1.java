package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.Prescriptions.PrescriptionResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.TimeSlotDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
import com.petclinic.bffapigateway.exceptions.ExistingPrescriptionNotFoundException;
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

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/visits")
public class VisitsControllerV1 {

    private final VisitsServiceClient visitsServiceClient;

    private final CustomersServiceClient customersServiceClient;

    /////////////////////////////////////////////
    ///////////Visits Methods///////////////////
    ///////////////////////////////////////////

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.RECEPTIONIST, Roles.OWNER})
    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<VisitResponseDTO>> getAllVisits(
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String petId,
            @RequestParam(required = false) String practitionerId,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false, defaultValue = "false") boolean archived) {

        if (status != null) {
            return ResponseEntity.ok().body(visitsServiceClient.getVisitsForStatus(status));
        }
        if (petId != null) {
            return ResponseEntity.ok().body(visitsServiceClient.getVisitsForPet(petId));
        }
        if (practitionerId != null) {
            return ResponseEntity.ok().body(visitsServiceClient.getVisitByPractitionerId(practitionerId));
        }
        if (ownerId != null) {
            return ResponseEntity.ok().body(customersServiceClient.getPetsByOwnerId(ownerId)
                    .flatMap(pet -> visitsServiceClient.getVisitsForPet(pet.getPetId())));
        }
        if (archived) {
            return ResponseEntity.ok().body(visitsServiceClient.getAllArchivedVisits());
        }

        return ResponseEntity.ok().body(visitsServiceClient.getAllVisits(description));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/{visitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> getVisitByVisitId(
            @PathVariable String visitId,
            @RequestParam(required = false, defaultValue = "false") boolean includePrescription) {

        return visitsServiceClient.getVisitByVisitId(visitId, includePrescription)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.OWNER, Roles.VET})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> addVisit(@RequestBody Mono<VisitRequestDTO> visitRequestDTO) {
        return visitsServiceClient.addVisit(visitRequestDTO)
                .map(v -> ResponseEntity.status(HttpStatus.CREATED).body(v))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.RECEPTIONIST})
    @PutMapping(value = "/{visitId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> updateVisitByVisitId(
            @PathVariable String visitId,
            @RequestBody Mono<VisitRequestDTO> visitRequestDTO) {
        return visitRequestDTO
                .flatMap(request -> visitsServiceClient.updateVisitByVisitId(visitId, Mono.just(request))
                        .map(updatedVisit -> ResponseEntity.ok(updatedVisit))
                        .defaultIfEmpty(ResponseEntity.notFound().build()));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.RECEPTIONIST})
    @PatchMapping(value = "/{visitId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> updateStatusForVisitByVisitId(
            @PathVariable String visitId,
            @RequestParam String status) {

        return visitsServiceClient.updateStatusForVisitByVisitId(visitId, status)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.RECEPTIONIST})
    @DeleteMapping("/{visitId}")
    public Mono<ResponseEntity<Void>> deleteVisitsByVisitId(@PathVariable String visitId) {

        return visitsServiceClient.deleteVisitByVisitId(visitId)
                .map(v -> ResponseEntity.noContent().<Void>build())
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @IsUserSpecific(idToMatch = {"visitId"})
    @PutMapping(value = "/{visitId}/archive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VisitResponseDTO>> archiveCompletedVisit(
            @PathVariable String visitId,
            @RequestBody Mono<VisitRequestDTO> visitRequestDTOMono) {
        return visitsServiceClient.archiveCompletedVisit(visitId, visitRequestDTOMono)
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
    ///////////Reviews Methods//////////////////
    ///////////////////////////////////////////

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/reviews", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<ReviewResponseDTO>> getAllReviews() {
        return ResponseEntity.ok().body(visitsServiceClient.getAllReviews());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @IsUserSpecific(idToMatch = {"reviewId"})
    @GetMapping(value = "/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> getReviewByReviewId(@PathVariable String reviewId) {
        return Mono.just(reviewId)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided review ID is invalid: " + reviewId)))
                .flatMap(id -> visitsServiceClient.getReviewByReviewId(id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping(value = "/reviews", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> PostReview(@RequestBody Mono<ReviewRequestDTO> reviewRequestDTOMono) {
        return visitsServiceClient.createReview(reviewRequestDTOMono)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @IsUserSpecific(idToMatch = {"reviewId"})
    @PutMapping(value = "/reviews/{reviewId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> updateReview(
            @PathVariable String reviewId,
            @RequestBody Mono<ReviewRequestDTO> reviewRequestDTO) {

        return Mono.just(reviewId)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided review ID is invalid: " + reviewId)))
                .flatMap(id -> visitsServiceClient.updateReview(id, reviewRequestDTO))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER, Roles.ADMIN})
    @DeleteMapping(value = "/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping(value="/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping(value = "/reviews/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ReviewResponseDTO>> deleteReview(@PathVariable String reviewId) {
        return Mono.just(reviewId)
                .flatMap(visitsServiceClient::deleteReview)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.OWNER})
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
    ///////////Availability Methods//////////////
    ///////////////////////////////////////////

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.OWNER, Roles.VET})
    @GetMapping(value = "/availability/vets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Flux<VetResponseDTO>> getAllVetsForAvailability() {
        return ResponseEntity.ok().body(visitsServiceClient.getAllVetsForAvailability());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST, Roles.OWNER, Roles.VET})
    @GetMapping(value = "/availability/vets/{vetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> getVeterinarianAvailability(
            @PathVariable String vetId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        if (date != null && startDate == null && endDate == null) {
            return visitsServiceClient.getAvailableTimeSlots(vetId, date)
                    .collectList()
                    .map(slots -> ResponseEntity.ok(VetResponseDTO.builder().build()))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        }

        if (startDate != null && endDate != null) {
            return visitsServiceClient.getAvailableDates(vetId, startDate, endDate)
                    .collectList()
                    .map(dates -> ResponseEntity.ok(VetResponseDTO.builder().build()))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        }

        return visitsServiceClient.getVeterinarianAvailability(vetId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
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

    /////////////////////////////////////////////
    /////////// Prescription Methods ////////////
    /////////////////////////////////////////////

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PostMapping(
            value = "/{visitId}/prescriptions",
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

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.OWNER})
    @GetMapping(value = "/{visitId}/prescription/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadPrescriptionPdf(
            @PathVariable String visitId) {

        return visitsServiceClient.downloadPrescriptionPdf(visitId)
                .map(pdfBytes -> new ResponseEntity<>(pdfBytes, HttpStatus.OK))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
