package com.petclinic.bffapigateway.presentationlayer;


import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.PaymentRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.*;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.*;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import com.petclinic.bffapigateway.utils.VetsEntityDtoUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

/**
 * @author Maciej Szarlinski
 * @author Christine Gerard
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 * Modified to remove circuitbreaker
 */

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/gateway")
@Validated
public class BFFApiGatewayController {

    private final CustomersServiceClient customersServiceClient;

    private final VisitsServiceClient visitsServiceClient;

    private final VetsServiceClient vetsServiceClient;

    private final AuthServiceClient authServiceClient;

    private final BillServiceClient billServiceClient;

    private final InventoryServiceClient inventoryServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "bills/{billId}")
    public Mono<ResponseEntity<BillResponseDTO>> getBillById(final @PathVariable String billId)
    {
        return billServiceClient.getBillById(billId)
                .map(updated -> ResponseEntity.status(HttpStatus.OK).body(updated))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.VET, Roles.ADMIN})
    @PostMapping(value = "bills",
            consumes = "application/json",
            produces = "application/json")
    public Mono<ResponseEntity<BillResponseDTO>> createBill(@RequestBody BillRequestDTO model) {
        return billServiceClient.createBill(model).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "bills", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBills() {
        return billServiceClient.getAllBills();
    }

    //to be changed
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @GetMapping(value = "bills/bills-pagination", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Flux<BillResponseDTO> getAllBillsByPage(@RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size,
//                                                     @RequestParam(required = false) String billId,
//                                                     @RequestParam(required = false) String customerId,
//                                                     @RequestParam(required = false) String ownerFirstName,
//                                                     @RequestParam(required = false) String ownerLastName,
//                                                     @RequestParam(required = false) String visitType,
//                                                     @RequestParam(required = false) String vetId,
//                                                     @RequestParam(required = false) String vetFirstName,
//                                                     @RequestParam(required = false) String vetLastName) {
//        if(page.isEmpty()){
//            page = Optional.of(0);
//        }
//
//        if (size.isEmpty()) {
//            size = Optional.of(5);
//        }
//
//        return billServiceClient.getAllBillsByPage(page, size, billId, customerId, ownerFirstName, ownerLastName, visitType,
//                vetId, vetFirstName, vetLastName);
//    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "bills", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BillResponseDTO> getAllBillsByPage(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam(required = false) String billId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String ownerFirstName,
            @RequestParam(required = false) String ownerLastName,
            @RequestParam(required = false) String visitType,
            @RequestParam(required = false) String vetId,
            @RequestParam(required = false) String vetFirstName,
            @RequestParam(required = false) String vetLastName) {

        if(page.isEmpty()){
            page = Optional.of(0);
        }

        if (size.isEmpty()) {
            size = Optional.of(10);
        }

        return billServiceClient.getAllBillsByPage(page, size, billId, customerId, ownerFirstName, ownerLastName,
                visitType, vetId, vetFirstName, vetLastName);
    }


    //to be changed
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "bills/bills-count")
    public Mono<Long> getTotalNumberOfBills(){
        return billServiceClient.getTotalNumberOfBills();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "bills/bills-filtered-count")
    public Mono<Long> getTotalNumberOfBillsWithFilters (@RequestParam(required = false) String billId,
                                                        @RequestParam(required = false) String customerId,
                                                        @RequestParam(required = false) String ownerFirstName,
                                                        @RequestParam(required = false) String ownerLastName,
                                                        @RequestParam(required = false) String visitType,
                                                        @RequestParam(required = false) String vetId,
                                                        @RequestParam(required = false) String vetFirstName,
                                                        @RequestParam(required = false) String vetLastName)
    {
        return billServiceClient.getTotalNumberOfBillsWithFilters(billId, customerId, ownerFirstName, ownerLastName, visitType,
                vetId, vetFirstName, vetLastName);
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "bills/paid", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllPaidBills() {
        return billServiceClient.getAllPaidBills();
    }
    
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "bills/unpaid", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllUnpaidBills() {
        return billServiceClient.getAllUnpaidBills();
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "bills/overdue", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllOverdueBills() {
        return billServiceClient.getAllOverdueBills();
    }


    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping(value = "bills/customer/{customerId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByOwnerId(final @PathVariable String customerId)
    {
        return billServiceClient.getBillsByOwnerId(customerId);
    }

    @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping(value = "bills/vets/{vetId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByVetId(final @PathVariable String vetId)
    {
        return billServiceClient.getBillsByVetId(vetId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "bills/owner/{ownerFirstName}/{ownerLastName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBillsByOwnerName(@PathVariable String ownerFirstName, @PathVariable String ownerLastName) {
        return billServiceClient.getBillsByOwnerName(ownerFirstName, ownerLastName);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "bills/vet/{vetFirstName}/{vetLastName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBillsByVetName(@PathVariable String vetFirstName, @PathVariable String vetLastName) {
        return billServiceClient.getBillsByVetName(vetFirstName, vetLastName);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "bills/visitType/{visitType}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBillsByVisitType(@PathVariable String visitType) {
        return billServiceClient.getBillsByVisitType(visitType);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PutMapping("/bills/{billId}")
    public Mono<ResponseEntity<BillResponseDTO>> updateBill(@PathVariable String billId, @RequestBody Mono<BillRequestDTO> billRequestDTO){
        return billServiceClient.updateBill(billId, billRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "bills")
    public Mono<Void> deleteAllBills(){
        return billServiceClient.deleteAllBills();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "bills/{billId}")
    public Mono<ResponseEntity<Void>> deleteBill(final @PathVariable String billId){
        return billServiceClient.deleteBill(billId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @DeleteMapping(value = "bills/vets/{vetId}")
    public Mono<ResponseEntity<Void>> deleteBillsByVetId(final @PathVariable String vetId){
        return billServiceClient.deleteBillsByVetId(vetId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "bills/customer/{customerId}")
    public Mono<ResponseEntity<Void>> deleteBillsByCustomerId(final @PathVariable String customerId){
        return billServiceClient.deleteBillsByCustomerId(customerId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Validated
    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN})
    @PostMapping("bills/customer/{customerId}/bills/{billId}/pay")
    public Mono<ResponseEntity<BillResponseDTO>> payBill(
            @PathVariable("customerId") String customerId,
            @PathVariable("billId") String billId,
            @Valid @RequestBody PaymentRequestDTO paymentRequestDTO) {
        return billServiceClient.payBill(customerId, billId, paymentRequestDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
    @PostMapping(value = "/owners/{ownerId}/pets" , produces = "application/json", consumes = "application/json")
    public Mono<ResponseEntity<PetResponseDTO>> createPetForOwner(@PathVariable String ownerId, @RequestBody PetRequestDTO petRequest){
        return customersServiceClient.createPetForOwner(ownerId, petRequest)
                .map(pet -> ResponseEntity.status(HttpStatus.CREATED).body(pet))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER,Roles.ADMIN,Roles.VET})
    @PatchMapping(value = "/pet/{petId}", produces = "application/json", consumes = "application/json")
    public Mono<ResponseEntity<PetResponseDTO>> patchPet(@RequestBody PetRequestDTO pet, @PathVariable String petId) {
        return customersServiceClient.patchPet(pet, petId).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/pets", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PetResponseDTO> getAllPets(){
        return customersServiceClient.getAllPets();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> getPetByPetId(@PathVariable String petId){
        return customersServiceClient.getPetByPetId(petId).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "owners/{ownerId}/pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> getPet(@PathVariable String ownerId, @PathVariable String petId){
        return customersServiceClient.getPet(ownerId, petId).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/owners/{ownerId}/pets", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PetResponseDTO> getPetsByOwnerId(@PathVariable String ownerId){
        return customersServiceClient.getPetsByOwnerId(ownerId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @DeleteMapping("owners/{ownerId}/pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> deletePet(@PathVariable String ownerId, @PathVariable String petId){
        return customersServiceClient.deletePet(ownerId,petId).then(Mono.just(ResponseEntity.noContent().<PetResponseDTO>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @DeleteMapping("pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> deletePetByPetId(@PathVariable String petId){
        return customersServiceClient.deletePetByPetId(petId).then(Mono.just(ResponseEntity.noContent().<PetResponseDTO>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "owners/petTypes", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PetType> getPetTypes(){
        return customersServiceClient.getPetTypes();
    }
    
    /*
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @PutMapping("pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> updatePet(@RequestBody PetResponseDTO pet, @PathVariable String petId){
        return customersServiceClient.updatePet(pet, petId).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

*/

//        /* Visits Methods */
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @GetMapping(value = "reviews", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ReviewResponseDTO> getAllReviews(){
//        return visitsServiceClient.getAllReviews();
//    }
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @GetMapping(value = "visits", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<VisitResponseDTO> getAllVisits(@RequestParam(required = false) String description){
//        return visitsServiceClient.getAllVisits(description);
//    }
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.OWNER})
//    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
//    @GetMapping(value = "visits/owners/{ownerId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<VisitResponseDTO> getVisitsByOwnerId(@PathVariable String ownerId){
////not ideal since returns complete pet dto
//        return getPetsByOwnerId(ownerId).flatMap(petResponseDTO -> getVisitsForPet(petResponseDTO.getPetId()));
//    }
//
//
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.OWNER})
//    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
//    @GetMapping(value = "visits/emergency/owners/{ownerId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<EmergencyResponseDTO> getEmergencyVisitsByOwnerId(@PathVariable String ownerId){
////not ideal since returns complete pet dto
//        return getPetsByOwnerId(ownerId).flatMap(petResponseDTO -> getEmergencyVisitsForPet(petResponseDTO.getPetId()));
//    }
//
//    @GetMapping(value = "visits/vets/{practitionerId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<VisitResponseDTO> getVisitByPractitionerId(@PathVariable String practitionerId){
//        return visitsServiceClient.getVisitByPractitionerId(practitionerId);
//    }
//
//    @GetMapping(value = "visits/pets/{petId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<VisitResponseDTO> getVisitsForPet(@PathVariable String petId){
//        return visitsServiceClient.getVisitsForPet(petId);
//    }
//
//    @GetMapping(value = "visits/emergency/pets/{petId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<EmergencyResponseDTO> getEmergencyVisitsForPet(@PathVariable String petId){
//        return visitsServiceClient.getEmergencyVisitForPet(petId);
//    }
//
//    @GetMapping(value = "visits/status/{status}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<VisitResponseDTO> getVisitsForStatus(@PathVariable String status){
//        return visitsServiceClient.getVisitsForStatus(status);
//    }
//
//    @GetMapping(value ="visits/{visitId}")
//    public Mono<VisitResponseDTO> getVisitByVisitId(@PathVariable String visitId){
//        return visitsServiceClient.getVisitByVisitId(visitId);
//    }
//    @PostMapping(value = "visit/owners/{ownerId}/pets/{petId}/visits", consumes = "application/json", produces = "application/json")
//    Mono<ResponseEntity<VisitResponseDTO>> addVisit(@RequestBody VisitRequestDTO visit, @PathVariable String ownerId, /*@PathVariable String petId,*/ @CookieValue("Bearer") String auth) {
//        visit.setOwnerId(ownerId);
//        visit.setJwtToken(auth);
//        return visitsServiceClient.createVisitForPet(visit).map(ResponseEntity.status(HttpStatus.CREATED)::body)
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
//    @PutMapping(value = "/visits/{visitId}/status/{status}")
//    Mono<VisitResponseDTO> updateStatusForVisitByVisitId(@PathVariable String visitId, @PathVariable String status) {
//        return visitsServiceClient.updateStatusForVisitByVisitId(visitId, status);
//    }
//    @DeleteMapping (value = "visits/{visitId}")
//    public Mono<ResponseEntity<Void>> deleteVisitsByVisitId(@PathVariable String visitId){
//        return visitsServiceClient.deleteVisitByVisitId(visitId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
//
//    @DeleteMapping(value = "visits/cancelled")
//    public Mono<ResponseEntity<Void>> deleteAllCancelledVisits(){
//        return visitsServiceClient.deleteAllCancelledVisits().then(Mono.just(ResponseEntity.noContent().<Void>build()))
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
//    //        @PutMapping(value = "owners/*/pets/{petId}/visits/{visitId}", consumes = "application/json", produces = "application/json")
//    /*
//        Mono<ResponseEntity<VisitDetails>> updateVisit(@RequestBody VisitDetails visit, @PathVariable String petId, @PathVariable String visitId) {
//            visit.setPetId(petId);
//            visit.setVisitId(visitId);
//            return visitsServiceClient.updateVisitForPet(visit).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//        }
//
//        @GetMapping(value = "visits/previous/{petId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
//        public Flux<VisitDetails> getPreviousVisitsForPet(@PathVariable final String petId) {
//            return visitsServiceClient.getPreviousVisitsForPet(petId);
//        }
//        @GetMapping(value = "visits/scheduled/{petId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
//        public Flux<VisitDetails> getScheduledVisitsForPet(@PathVariable final String petId) {
//            return visitsServiceClient.getScheduledVisitsForPet(petId);
//        }
//
//        @GetMapping(value = "visits/calendar/{practitionerId}")
//        public Flux<VisitDetails> getVisitsByPractitionerIdAndMonth(@PathVariable("practitionerId") int practitionerId, @RequestParam("dates") List<String> dates) {
//            String startDate = dates.get(0);
//            String endDate = dates.get(1);
//            return visitsServiceClient.getVisitsByPractitionerIdAndMonth(practitionerId, startDate, endDate);
//        }
//        private Function<Visits, OwnerResponseDTO> addVisitsToOwner(OwnerResponseDTO owner) {
//            return visits -> {
//                owner.getPets()
//                        .forEach(pet -> pet.getVisits()
//                                .addAll(visits.getItems().stream()
//                                        .filter(v -> v.getPetId() == pet.getId())
//                                        .collect(Collectors.toList()))
//                        );
//                return owner;
//            };
//        }
//*/
//
//    @PostMapping(value = "visit/owners/5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd/pets/9/visits", consumes = "application/json", produces = "application/json")
//    Mono<ResponseEntity<VisitResponseDTO>> addVisit(@RequestBody VisitRequestDTO visit/* @PathVariable String ownerId, @PathVariable String petId*/) {
//       // visit.setPetId(petId);
//        return visitsServiceClient.createVisitForPet(visit).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }
//
////    @PutMapping(value = "owners/*/pets/{petId}/visits/{visitId}", consumes = "application/json", produces = "application/json")
////    Mono<ResponseEntity<VisitDetails>> updateVisit(@RequestBody VisitDetails visit, @PathVariable String petId, @PathVariable String visitId) {
////        visit.setPetId(petId);
////        visit.setVisitId(visitId);
////        return visitsServiceClient.updateVisitForPet(visit).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
////                .defaultIfEmpty(ResponseEntity.badRequest().build());
////    }
//    /*  End of Visit Methods */
//
//    /**
//     * End of Visit Methods
//     **/

    /**
     * Start of Vet Methods
     **/

    //Photo
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("vets/{vetId}/photo")
    public Mono<ResponseEntity<Resource>> getPhotoByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getPhotoByVetId(vetId)
                .map(r -> ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE).body(r))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PostMapping(value = "{vetId}/specialties")
    public Mono<VetResponseDTO> addSpecialtiesByVetId(
            @PathVariable String vetId,
            @RequestBody Mono<SpecialtyDTO> specialties) {
        return vetsServiceClient.addSpecialtiesByVetId(vetId, specialties);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("vets/{vetId}/default-photo")
    public Mono<ResponseEntity<PhotoResponseDTO>> getDefaultPhotoByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getDefaultPhotoByVetId(vetId)
                .map(r -> ResponseEntity.ok().body(r))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PostMapping(
            value = "vets/{vetId}/photos",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public Mono<ResponseEntity<Resource>> addPhotoByVetId(
            @PathVariable String vetId,
            @RequestHeader("Photo-Name") String photoName,
            @RequestBody Mono<byte[]> fileData) {

        return fileData
                .flatMap(bytes -> vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, bytes))
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PostMapping(
            value = "vets/{vetId}/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Mono<ResponseEntity<Resource>> addPhotoByVetIdMultipart(
            @PathVariable String vetId,
            @RequestPart("photoName") String photoName,
            @RequestPart("file") Mono<FilePart> file) {

        return file
                .flatMap(fp -> vetsServiceClient.addPhotoToVet(vetId, photoName, fp))
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping(value = "vets/{vetId}/photos/{photoName}")
    public Mono<ResponseEntity<Resource>> updatePhotoByVetId(@PathVariable String vetId, @PathVariable String photoName, @RequestBody Mono<Resource> image) {
        return vetsServiceClient.updatePhotoOfVet(vetId, photoName, image)
                .map(p -> ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE).body(p))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    //Badge
    @GetMapping("vets/{vetId}/badge")
    public Mono<ResponseEntity<BadgeResponseDTO>> getBadgeByVetId(@PathVariable String vetId){
        return vetsServiceClient.getBadgeByVetId(vetId)
                .map(r->ResponseEntity.status(HttpStatus.OK).body(r))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    //Ratings
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "vets/{vetId}/ratings")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<RatingResponseDTO> getRatingsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getRatingsByVetId(VetsEntityDtoUtil.verifyId(vetId));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("/vets/{vetId}/ratings/count")
    public Mono<ResponseEntity<Integer>> getNumberOfRatingsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getNumberOfRatingsByVetId(VetsEntityDtoUtil.verifyId(vetId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "vets/{vetId}/ratings")
    public Mono<ResponseEntity<RatingResponseDTO>> addRatingToVet(@PathVariable String vetId, @RequestBody Mono<RatingRequestDTO> ratingRequestDTO) {
        return vetsServiceClient.addRatingToVet(vetId, ratingRequestDTO)
                .map(r->ResponseEntity.status(HttpStatus.CREATED).body(r))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "vets/{vetId}/ratings/{ratingId}")
    public Mono<ResponseEntity<Void>> deleteRatingByRatingId(@PathVariable String vetId,
                                             @PathVariable String ratingId){
        return vetsServiceClient.deleteRating(vetId,ratingId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("/vets/topVets")
    public Flux<VetAverageRatingDTO>getTopThreeVetsWithHighestAverageRating(){
        return vetsServiceClient.getTopThreeVetsWithHighestAverageRating();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("vets/{vetId}/ratings/date")
    public Flux<RatingResponseDTO> getRatingsOfAVetBasedOnDate(@PathVariable String vetId, @RequestParam Map<String,String> queryParams){
        return vetsServiceClient.getRatingsOfAVetBasedOnDate(vetId,queryParams);
    }

    @GetMapping(value = "vets/{vetId}/ratings/average")
    public Mono<ResponseEntity<Double>> getAverageRatingByVetId(@PathVariable String vetId){
        return vetsServiceClient.getAverageRatingByVetId(VetsEntityDtoUtil.verifyId(vetId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PutMapping(value="vets/{vetId}/ratings/{ratingId}")
    public Mono<ResponseEntity<RatingResponseDTO>> updateRatingByVetIdAndRatingId(@PathVariable String vetId,
                                                                                  @PathVariable String ratingId,
                                                                                  @RequestBody Mono<RatingRequestDTO> ratingRequestDTOMono){
        return vetsServiceClient.updateRatingByVetIdAndByRatingId(vetId, ratingId, ratingRequestDTOMono)
                .map(r->ResponseEntity.status(HttpStatus.OK).body(r))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("/vets/{vetId}/ratings/percentages")
    public Mono<ResponseEntity<String>> getPercentageOfRatingsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getPercentageOfRatingsByVetId(VetsEntityDtoUtil.verifyId(vetId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "vets/{vetId}/educations")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EducationResponseDTO> getEducationsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getEducationsByVetId(VetsEntityDtoUtil.verifyId(vetId));
    }

    @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @DeleteMapping(value = "vets/{vetId}/educations/{educationId}")
    public Mono<ResponseEntity<Void>> deleteEducationByEducationId(@PathVariable String vetId,
                                                   @PathVariable String educationId){
        return vetsServiceClient.deleteEducation(vetId,educationId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @PostMapping(value = "vets/{vetId}/educations")
    public Mono<ResponseEntity<EducationResponseDTO>> addEducationToAVet(@PathVariable String vetId, @RequestBody Mono<EducationRequestDTO> educationRequestDTOMono){
        return vetsServiceClient.addEducationToAVet(vetId, educationRequestDTOMono)
                .map(r->ResponseEntity.status(HttpStatus.CREATED).body(r))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
    @PutMapping(value="vets/{vetId}/educations/{educationId}")
    public Mono<ResponseEntity<EducationResponseDTO>> updateEducationByVetIdAndEducationId(@PathVariable String vetId,
                                                                                           @PathVariable String educationId,
                                                                                           @RequestBody Mono<EducationRequestDTO> educationRequestDTOMono){
        return vetsServiceClient.updateEducationByVetIdAndByEducationId(vetId, educationId, educationRequestDTOMono)
                .map(e->ResponseEntity.status(HttpStatus.OK).body(e))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    //Vets
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "vets")
    public Flux<VetResponseDTO> getAllVets() {
        return vetsServiceClient.getVets();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("/vets/{vetId}")
    public Mono<ResponseEntity<VetResponseDTO>> getVetByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getVetByVetId(VetsEntityDtoUtil.verifyId(vetId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"vetId"})
    @GetMapping("/vets/vetBillId/{vetId}")
    public Mono<ResponseEntity<VetResponseDTO>> getVetByBillId(@PathVariable String vetBillId) {
        return vetsServiceClient.getVetByVetBillId(VetsEntityDtoUtil.verifyId(vetBillId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping(value = "/vets/active")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VetResponseDTO> getActiveVets() {
        return vetsServiceClient.getActiveVets();
    }

    @GetMapping(value = "/vets/inactive")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VetResponseDTO> getInactiveVets() {
        return vetsServiceClient.getInactiveVets();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/users/vets",consumes = "application/json",produces = "application/json")
    public Mono<ResponseEntity<VetResponseDTO>> insertVet(@RequestBody Mono<RegisterVet> vetDTOMono) {
        return authServiceClient.createVetUser(vetDTOMono)
                .map(v->ResponseEntity.status(HttpStatus.CREATED).body(v))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @PutMapping(value = "/vets/{vetId}",consumes = "application/json",produces = "application/json")
    public Mono<ResponseEntity<VetResponseDTO>> updateVetByVetId(@PathVariable String vetId, @RequestBody Mono<VetRequestDTO> vetDTOMono) {
        return vetsServiceClient.updateVet(VetsEntityDtoUtil.verifyId(vetId), vetDTOMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/vets/{vetId}")
    public Mono<ResponseEntity<Void>> deleteVet(@PathVariable String vetId) {
        return vetsServiceClient.deleteVet(VetsEntityDtoUtil.verifyId(vetId))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * End of Vet Methods
     **/
//
//    @DeleteMapping(value = "users/{userId}")
//    public Mono<UserDetails> deleteUser(@RequestHeader(AUTHORIZATION) String auth, final @PathVariable long userId) {
//        return authServiceClient.deleteUser(auth, userId);
//    }
//
//    @GetMapping(value = "users/{userId}")
//    public Mono<UserDetails> getUserDetails(final @PathVariable long userId) {
//        return authServiceClient.getUser(userId);
//    }

//
//    @PutMapping(value = "users/{userId}",
//            consumes = "application/json",
//            produces = "application/json")
//    public Mono<UserDetails> updateUser(final @PathVariable long userId, @RequestBody Register model) {
//        return authServiceClient.updateUser(userId, model);
//    }

    /**
     * Beginning of Auth Methods
     **/
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("/verification/{token}")
    public Mono<ResponseEntity<UserDetails>> verifyUser(@PathVariable final String token) {
        return authServiceClient.verifyUser(token)
                .map(userDetailsResponseEntity -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Location", "http://localhost:8080/#!/login");
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .headers(headers)
                            .body(userDetailsResponseEntity.getBody());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "/users",
            consumes = "application/json",
            produces = "application/json")
    public Mono<ResponseEntity<OwnerResponseDTO>> createUser(@RequestBody @Valid Mono<Register> model) {
        return authServiceClient.createUser(model).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "users", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserDetails> getAllUsers(@CookieValue("Bearer") String auth, @RequestParam Optional<String> username) {
        if(username.isPresent()) {
            return authServiceClient.getUsersByUsername(auth, username.get());
        }
        else {
            return authServiceClient.getUsers(auth);
        }
    }

    @PatchMapping(value = "users/{userId}",
            consumes = "application/json",
            produces = "application/json")
    public Mono<ResponseEntity<UserResponseDTO>> updateUserRoles(final @PathVariable String userId, @RequestBody RolesChangeRequestDTO roleChangeDTO, @CookieValue("Bearer") String auth) {
        return authServiceClient.updateUsersRoles(userId, roleChangeDTO, auth)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserDetails> getUserById(@PathVariable String userId, @CookieValue("Bearer") String auth) {
        return authServiceClient.getUserById(auth, userId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> deleteUserById(@PathVariable String userId, @CookieValue("Bearer") String auth) {
        return authServiceClient.deleteUser(auth, userId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation()
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "/users/login",produces = "application/json;charset=utf-8;", consumes = "application/json")
    public Mono<ResponseEntity<UserPasswordLessDTO>> login(@RequestBody Mono<Login> login) throws Exception {
        log.info("Entered controller /login");
        return authServiceClient.login(login);

    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping("/users/logout")
    public Mono<ResponseEntity<Void>> logout(ServerHttpRequest request, ServerHttpResponse response) {
        return authServiceClient.logout(request, response);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "/users/forgot_password")
    public Mono<ResponseEntity<Void>> processForgotPassword(@RequestBody Mono<UserEmailRequestDTO> email) {
        return authServiceClient.sendForgottenEmail(email);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping("/users/reset_password")
    public Mono<ResponseEntity<Void>> processResetPassword(@RequestBody @Valid Mono<UserPasswordAndTokenRequestModel> resetRequest) {
        return authServiceClient.changePassword(resetRequest);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/users/inventoryManager")
    public Mono<ResponseEntity<UserPasswordLessDTO>> createInventoryManager(@RequestBody @Valid Mono<RegisterInventoryManager> model) {
        return authServiceClient.createInventoryMangerUser(model).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    /**
     * End of Auth Methods
     **/

}
