package com.petclinic.bffapigateway.presentationlayer;


import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.Auth.Login;
import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.Auth.UserPasswordLessDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.*;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.dtos.Visits.VisitDetails;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import com.petclinic.bffapigateway.utils.VetsEntityDtoUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<BillResponseDTO>> getBillingInfo(final @PathVariable String billId)
    {
        return billServiceClient.getBilling(billId)
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
    public Flux<BillResponseDTO> getAllBilling() {
        return billServiceClient.getAllBilling();
    }



    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping(value = "bills/customer/{customerId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByOwnerId(final @PathVariable String customerId)
    {
        return billServiceClient.getBillsByOwnerId(customerId);
    }

    @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping(value = "bills/vet/{vetId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByVetId(final @PathVariable String vetId)
    {
        return billServiceClient.getBillsByVetId(vetId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PutMapping("/bills/{billId}")
    public Mono<ResponseEntity<BillResponseDTO>> updateBill(@PathVariable String billId, @RequestBody Mono<BillRequestDTO> billRequestDTO){
        return billServiceClient.updateBill(billId, billRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "bills/{billId}")
    public Mono<ResponseEntity<Void>> deleteBill(final @PathVariable String billId){
        return billServiceClient.deleteBill(billId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @DeleteMapping(value = "bills/vet/{vetId}")
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



    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
    @PostMapping(value = "owners/{ownerId}/pets" , produces = "application/json", consumes = "application/json")
    public Mono<ResponseEntity<PetResponseDTO>> createPet(@RequestBody PetResponseDTO pet, @PathVariable String ownerId){
        return customersServiceClient.createPet(pet, ownerId).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


  /*@GetMapping(value = "owners/{ownerId}/pets")
   public Flux<PetResponseDTO> getAllPetsFromOwnerId(@PathVariable String ownerId){
        return customersServiceClient.getAllPets(ownerId);
    }*/


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


    @GetMapping(value = "owners/petTypes", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PetType> getPetTypes(){
        return customersServiceClient.getPetTypes();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @PutMapping("pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> updatePet(@RequestBody PetResponseDTO pet, @PathVariable String petId){
        return customersServiceClient.updatePet(pet, petId).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    /**
     * Visits Methods
     **/
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "visits", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getAllVisits() {
        return visitsServiceClient.getAllVisits();
    }

    @GetMapping(value = "visits/previous/{petId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitDetails> getPreviousVisitsForPet(@PathVariable final String petId) {
        return visitsServiceClient.getPreviousVisitsForPet(petId);
    }

    @GetMapping(value = "visits/scheduled/{petId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitDetails> getScheduledVisitsForPet(@PathVariable final String petId) {
        return visitsServiceClient.getScheduledVisitsForPet(petId);
    }

    @GetMapping(value = "visits/vets/{practitionerId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitDetails> getVisitForPractitioner(@PathVariable int practitionerId){
        return visitsServiceClient.getVisitForPractitioner(practitionerId);
    }

    /*
    @GetMapping(value = "visits/calendar/{practitionerId}")
    public Flux<VisitDetails> getVisitsByPractitionerIdAndMonth(@PathVariable("practitionerId") int practitionerId,
                                                                @RequestParam("dates") List<String> dates) {
        String startDate = dates.get(0);
        String endDate = dates.get(1);
        return visitsServiceClient.getVisitsByPractitionerIdAndMonth(practitionerId, startDate, endDate);
    }
     */
    @GetMapping(value = "visits/pets/{petId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitDetails> getVisitsForPet(final @PathVariable String petId){
        return visitsServiceClient.getVisitsForPet(petId);
    }

    @GetMapping(value = "visits/status/{status}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getVisitsForStatus(final @PathVariable String status){
        return visitsServiceClient.getVisitsForStatus(status);
    }

    @GetMapping(value ="visits/{visitId}")
    public Mono<VisitResponseDTO> getVisitByVisitId(final @PathVariable String visitId){
        return visitsServiceClient.getVisitByVisitId(visitId);
    }

    /*
    private Function<Visits, OwnerResponseDTO> addVisitsToOwner(OwnerResponseDTO owner) {
        return visits -> {
            owner.getPets()
                    .forEach(pet -> pet.getVisits()
                            .addAll(visits.getItems().stream()
                                    .filter(v -> v.getPetId() == pet.getId())
                                    .collect(Collectors.toList()))
                    );
            return owner;
        };
    }
    */



    @PostMapping(value = "visit/owners/5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd/pets/9/visits", consumes = "application/json", produces = "application/json")
    Mono<ResponseEntity<VisitResponseDTO>> addVisit(@RequestBody VisitRequestDTO visit/* @PathVariable String ownerId, @PathVariable String petId*/) {
       // visit.setPetId(petId);
        return visitsServiceClient.createVisitForPet(visit).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
    @PutMapping(value = "owners/*/pets/{petId}/visits/{visitId}", consumes = "application/json", produces = "application/json")
    Mono<ResponseEntity<VisitDetails>> updateVisit(@RequestBody VisitDetails visit, @PathVariable String petId, @PathVariable String visitId) {
        visit.setPetId(petId);
        visit.setVisitId(visitId);
        return visitsServiceClient.updateVisitForPet(visit).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping(value = "/visits/{visitId}/status/{status}")
    Mono<VisitResponseDTO> updateStatusForVisitByVisitId(@PathVariable String visitId, @PathVariable String status) {
        return visitsServiceClient.updateStatusForVisitByVisitId(visitId, status);
    }

    @DeleteMapping (value = "visits/{visitId}")
    public Mono<ResponseEntity<Void>> deleteVisitsByVisitId(final @PathVariable String visitId){
        return visitsServiceClient.deleteVisitByVisitId(visitId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "visits/cancelled")
    public Mono<ResponseEntity<Void>> deleteAllCancelledVisits(){
        return visitsServiceClient.deleteAllCancelledVisits().then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }


    /**
     * End of Visit Methods
     **/


    /**
     * Start of Vet Methods
     **/

    //Photo
    @IsUserSpecific(idToMatch = {"vetId"})
    @GetMapping("vets/{vetId}/photo")
    public Mono<ResponseEntity<Resource>> getPhotoByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getPhotoByVetId(vetId)
                .map(r -> ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE).body(r))
                .defaultIfEmpty(ResponseEntity.notFound().build());
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


    //Vets
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "vets")
    public Flux<VetDTO> getAllVets() {
        return vetsServiceClient.getVets();
    }

    @IsUserSpecific(idToMatch = {"vetId"})
    @GetMapping("/vets/{vetId}")
    public Mono<ResponseEntity<VetDTO>> getVetByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getVetByVetId(VetsEntityDtoUtil.verifyId(vetId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"vetId"})
    @GetMapping("/vets/vetBillId/{vetId}")
    public Mono<ResponseEntity<VetDTO>> getVetByVetBillId(@PathVariable String vetBillId) {
        return vetsServiceClient.getVetByVetBillId(VetsEntityDtoUtil.verifyId(vetBillId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping(value = "/vets/active")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VetDTO> getActiveVets() {
        return vetsServiceClient.getActiveVets();
    }

    @GetMapping(value = "/vets/inactive")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VetDTO> getInactiveVets() {
        return vetsServiceClient.getInactiveVets();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/users/vets",consumes = "application/json",produces = "application/json")
    public Mono<ResponseEntity<VetDTO>> insertVet(@RequestBody Mono<RegisterVet> vetDTOMono) {
        return authServiceClient.createVetUser(vetDTOMono)
                .map(v->ResponseEntity.status(HttpStatus.CREATED).body(v))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @PutMapping(value = "/vets/{vetId}",consumes = "application/json",produces = "application/json")
    public Mono<ResponseEntity<VetDTO>> updateVetByVetId(@PathVariable String vetId, @RequestBody Mono<VetDTO> vetDTOMono) {
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
     * Owners Methods
     **/

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "owners")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OwnerResponseDTO> getAllOwners() {
        return customersServiceClient.getAllOwners();
                /*.flatMap(n ->
                        visitsServiceClient.getVisitsForPets(n.getPetIds())
                                .map(addVisitsToOwner(n))
                );*/
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/owners-pagination")
    public Flux<OwnerResponseDTO> getOwnersByPagination(@RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) {

        if(page.isEmpty()){
            page = Optional.of(0);
        }

        if (size.isEmpty()) {
            size = Optional.of(5);
        }

        return customersServiceClient.getOwnersByPagination(page,size);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/owners-count")
    public Mono<Long> getTotalNumberOfOwners(){
        return customersServiceClient.getTotalNumberOfOwners();
    }


    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping(value = "owners/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> getOwnerDetails(final @PathVariable String ownerId) {
        return customersServiceClient.getOwner(ownerId)
                .map(ownerResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(ownerResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());

                /*.flatMap(owner ->
                        visitsServiceClient.getVisitsForPets(owner.getPetIds())
                                .map(addVisitsToOwner(owner))
                );*/
    }


//    @PostMapping(value = "owners",
//            consumes = "application/json",
//            produces = "application/json")
//    public Mono<OwnerResponseDTO> createOwner(@RequestBody OwnerResponseDTO model){
//        return customersServiceClient.createOwner(model);
//    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
    @PostMapping(value = "owners/photo/{ownerId}")
    public Mono<ResponseEntity<String>> setOwnerPhoto(@RequestBody PhotoDetails photoDetails, @PathVariable int ownerId) {
        return customersServiceClient.setOwnerPhoto(photoDetails, ownerId).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    /*@GetMapping(value = "owners/photo/{ownerId}")
    public Mono<PhotoDetails> getOwnerPhoto(@PathVariable int ownerId) {
        return customersServiceClient.getOwnerPhoto(ownerId);
    }*/

//    @PostMapping(value = "owners/{ownerId}/pet/photo/{petId}")
//    public Mono<String> setPetPhoto(@PathVariable String ownerId, @RequestBody PhotoDetails photoDetails, @PathVariable String petId) {
//        return customersServiceClient.setPetPhoto(ownerId, photoDetails, petId);
//    }
//
//    @GetMapping(value = "owners/{ownerId}/pet/photo/{petId}")
//    public Mono<PhotoDetails> getPetPhoto(@PathVariable String ownerId, @PathVariable String petId) {
//        return customersServiceClient.getPetPhoto(ownerId, petId);
//    }
//
//    @DeleteMapping(value = "owners/photo/{photoId}")
//    public Mono<Void> deleteOwnerPhoto(@PathVariable int photoId){
//        return customersServiceClient.deleteOwnerPhoto(photoId);
//    }
//
//    @DeleteMapping(value = "owners/{ownerId}/pet/photo/{photoId}")
//    public Mono<Void> deletePetPhoto(@PathVariable int ownerId, @PathVariable int photoId){
//        return customersServiceClient.deletePetPhoto(ownerId, photoId);
//    }





    /*

    // Endpoint to update an owner
    @PutMapping("owners/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> updateOwner(
            @PathVariable String ownerId,
            @RequestBody OwnerRequestDTO ownerRequestDTO) {
        return customersServiceClient.updateOwner(ownerId, ownerRequestDTO)
                .map(updatedOwner -> ResponseEntity.ok().body(updatedOwner))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


     */

    @IsUserSpecific(idToMatch = {"ownerId"})
    @PutMapping("owners/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> updateOwner(
            @PathVariable String ownerId,
            @RequestBody Mono<OwnerRequestDTO> ownerRequestMono) {
        return ownerRequestMono.flatMap(ownerRequestDTO ->
                customersServiceClient.updateOwner(ownerId, Mono.just(ownerRequestDTO))
                        .map(updatedOwner -> ResponseEntity.ok().body(updatedOwner))
                        .defaultIfEmpty(ResponseEntity.notFound().build())
        );
    }








    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "owners/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> deleteOwner(@PathVariable String ownerId){
        return customersServiceClient.deleteOwner(ownerId).then(Mono.just(ResponseEntity.noContent().<OwnerResponseDTO>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    /**
     * End of Owner Methods
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
    public Flux<UserDetails> getAllUsers(@CookieValue("Bearer") String auth) {
        return authServiceClient.getUsers(auth);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "/users/login",produces = "application/json;charset=utf-8;", consumes = "application/json")
    public Mono<ResponseEntity<UserPasswordLessDTO>> login(@RequestBody Mono<Login> login) throws Exception {
        log.info("Entered controller /login");
        return authServiceClient.login(login);

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


    //Start of Inventory Methods
    @PostMapping(value = "inventory/{inventoryId}/products")
    public Mono<ResponseEntity<ProductResponseDTO>> addProductToInventory(@RequestBody ProductRequestDTO model, @PathVariable String inventoryId){
        return inventoryServiceClient.addProductToInventory(model, inventoryId)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @PostMapping(value = "inventory")
    public Mono<ResponseEntity<InventoryResponseDTO>> addInventory(@RequestBody InventoryRequestDTO model){
        return inventoryServiceClient.addInventory(model)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})

    @GetMapping(value ="inventory/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> getInventoryById(@PathVariable String inventoryId){
        return inventoryServiceClient.getInventoryById(inventoryId)
                .map(inventory -> ResponseEntity.status(HttpStatus.OK).body(inventory))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    @GetMapping(value ="inventory/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>> getProductByProductIdInInventory(@PathVariable String inventoryId, @PathVariable String productId){
        return inventoryServiceClient.getProductByProductIdInInventory(inventoryId, productId)
                .map(product ->ResponseEntity.status(HttpStatus.OK).body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @PutMapping(value = "inventory/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> updateInventory( @RequestBody InventoryRequestDTO model, @PathVariable String inventoryId) {
        return inventoryServiceClient.updateInventory(model, inventoryId)
                .map(updatedStudent -> ResponseEntity.status(HttpStatus.OK).body(updatedStudent))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }



    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @PutMapping(value = "inventory/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInInventory(@RequestBody ProductRequestDTO model, @PathVariable String inventoryId, @PathVariable String productId){
        return inventoryServiceClient.updateProductInInventory(model, inventoryId, productId).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "inventory/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<Void>> deleteProductInInventory(@PathVariable String inventoryId, @PathVariable String productId){
        return inventoryServiceClient.deleteProductInInventory(inventoryId, productId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    @GetMapping(value = "inventory/{inventoryId}/products")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndFields(@PathVariable String inventoryId,
                                                                                                 @RequestParam(required = false) String productName,
                                                                                                 @RequestParam(required = false) Double productPrice,
                                                                                                 @RequestParam(required = false) Integer productQuantity){
        return inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductsField(inventoryId, productName, productPrice, productQuantity);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    @GetMapping(value = "inventory")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<InventoryResponseDTO> searchInventory(@RequestParam(required = false) String inventoryName,
                                                      @RequestParam(required = false) String inventoryType,
                                                      @RequestParam(required = false) String inventoryDescription){
        return inventoryServiceClient.searchInventory(inventoryName, inventoryType, inventoryDescription);
    }
    /*
    @GetMapping(value = "inventory")
    public Flux<InventoryResponseDTO> getAllInventory(){
        return inventoryServiceClient.getAllInventory();
    }

     */


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "inventory/{inventoryId}/products")
    public Mono<ResponseEntity<Void>> deleteAllProductsFromInventory(@PathVariable String inventoryId) {
        return inventoryServiceClient.deleteAllProductForInventory(inventoryId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "inventory")
    public Mono<ResponseEntity<Void>> deleteAllInventories() {
        return inventoryServiceClient.deleteAllInventories().then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @PostMapping(value = "inventory/type")
    public Mono<ResponseEntity<InventoryTypeResponseDTO>> addInventoryType(@RequestBody InventoryTypeRequestDTO inventoryTypeRequestDTO){
        return inventoryServiceClient.addInventoryType(inventoryTypeRequestDTO)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "inventory/{inventoryId}")
    public Mono<Void> deleteInventoryByInventoryId(@PathVariable String inventoryId) {
        return inventoryServiceClient.deleteInventoryByInventoryId(inventoryId);
    }
}
