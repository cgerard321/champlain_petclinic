package com.petclinic.bffapigateway.presentationlayer;


import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.Auth.Login;
import com.petclinic.bffapigateway.dtos.Auth.UserPasswordLessDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductResponseDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import com.petclinic.bffapigateway.dtos.Vets.RatingRequestDTO;
import com.petclinic.bffapigateway.dtos.Vets.PhotoDetails;
import com.petclinic.bffapigateway.dtos.Vets.RatingResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.dtos.Visits.VisitDetails;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import com.petclinic.bffapigateway.utils.VetsEntityDtoUtil;
import io.netty.handler.codec.http.cookie.Cookie;
import io.swagger.annotations.ResponseHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

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
public class BFFApiGatewayController {

    private final CustomersServiceClient customersServiceClient;

    private final VisitsServiceClient visitsServiceClient;

    private final VetsServiceClient vetsServiceClient;

    private final AuthServiceClient authServiceClient;

    private final BillServiceClient billServiceClient;

    private final InventoryServiceClient inventoryServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "bills/{billId}")
    public Mono<BillResponseDTO> getBillingInfo(final @PathVariable String billId)
    {
        return billServiceClient.getBilling(billId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "bills",
            consumes = "application/json",
            produces = "application/json")
    public Mono<BillResponseDTO> createBill(@RequestBody BillRequestDTO model) {
        return billServiceClient.createBill(model);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "bills")
    public Flux<BillResponseDTO> getAllBilling() {
        return billServiceClient.getAllBilling();
    }



    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @GetMapping(value = "bills/customer/{customerId}")
    public Flux<BillResponseDTO> getBillsByOwnerId(final @PathVariable int customerId)
    {
        return billServiceClient.getBillsByOwnerId(customerId);
    }

    @GetMapping(value = "bills/vet/{vetId}")
    public Flux<BillResponseDTO> getBillsByVetId(final @PathVariable String vetId)
    {
        return billServiceClient.getBillsByVetId(vetId);
    }

    @DeleteMapping(value = "bills/{billId}")
    public Mono<Void> deleteBill(final @PathVariable String billId){
        return billServiceClient.deleteBill(billId);
    }

    @DeleteMapping(value = "bills/vet/{vetId}")
    public Flux<Void> deleteBillsByVetId(final @PathVariable String vetId){
        return billServiceClient.deleteBillsByVetId(vetId);
    }

    @DeleteMapping(value = "bills/customer/{customerId}")
    public Flux<Void> deleteBillsByCustomerId(final @PathVariable int customerId){
        return billServiceClient.deleteBillsByCustomerId(customerId);
    }



    @PostMapping(value = "owners/{ownerId}/pets" , produces = "application/json", consumes = "application/json")
    public Mono<PetResponseDTO> createPet(@RequestBody PetResponseDTO pet, @PathVariable String ownerId){
        return customersServiceClient.createPet(pet, ownerId);
    }

    @GetMapping(value = "owners/{ownerId}/pets/{petId}")
    public Mono<PetResponseDTO> getPet(@PathVariable int ownerId, @PathVariable int petId){
        return customersServiceClient.getPet(ownerId, petId);
    }

    @DeleteMapping("owners/{ownerId}/pets/{petId}")
    public Mono<PetResponseDTO> deletePet(@PathVariable String ownerId, @PathVariable int petId){
        return customersServiceClient.deletePet(ownerId,petId);
    }

    @GetMapping("owners/petTypes")
    public Flux<PetType> getPetTypes(){
        return customersServiceClient.getPetTypes();
    }


    /**
     * Visits Methods
     * **/
    @GetMapping(value = "visits", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VisitResponseDTO> getAllVisits() {return visitsServiceClient.getAllVisits();}
    @GetMapping(value = "visits/previous/{petId}")
    public Flux<VisitDetails> getPreviousVisitsForPet(@PathVariable final int petId) {
        return visitsServiceClient.getPreviousVisitsForPet(petId);
    }

    @GetMapping(value = "visits/scheduled/{petId}")
    public Flux<VisitDetails> getScheduledVisitsForPet(@PathVariable final int petId) {
        return visitsServiceClient.getScheduledVisitsForPet(petId);
    }

    @GetMapping(value = "visits/vets/{practitionerId}")
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
    @GetMapping(value = "visits/pets/{petId}")
    public Flux<VisitDetails> getVisitsForPet(final @PathVariable int petId){
        return visitsServiceClient.getVisitsForPet(petId);
    }

    @GetMapping(value ="visits/{visitId}")
    public Mono<VisitResponseDTO>  getVisitByVisitId(final @PathVariable String visitId){
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

    @PostMapping(value = "visit/owners/{ownerId}/pets/{petId}/visits", consumes = "application/json", produces = "application/json")
    Mono<VisitDetails> addVisit(@RequestBody VisitDetails visit, @PathVariable String ownerId, @PathVariable String petId) {
        visit.setPetId(Integer.parseInt(petId));
        return visitsServiceClient.createVisitForPet(visit);
    }
    @PutMapping(value = "owners/*/pets/{petId}/visits/{visitId}", consumes = "application/json", produces = "application/json")
    Mono<VisitDetails> updateVisit(@RequestBody VisitDetails visit, @PathVariable int petId, @PathVariable String visitId) {
        visit.setPetId(petId);
        visit.setVisitId(visitId);
        return visitsServiceClient.updateVisitForPet(visit);
    }
    @DeleteMapping (value = "visits/{visitId}")
    public Mono<Void> deleteVisitsByVisitId(final @PathVariable String visitId){
        return visitsServiceClient.deleteVisitByVisitId(visitId);
    }

    @GetMapping(value = "vets")
    public Flux<VetDTO> getAllVets() {
        return vetsServiceClient.getVets();
    }

    @GetMapping(value = "vets/{vetId}/ratings")
    public Flux<RatingResponseDTO> getRatingsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getRatingsByVetId(vetId);
    }

    @GetMapping("/vets/{vetId}/ratings/count")
    public Mono<ResponseEntity<Integer>> getNumberOfRatingsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getNumberOfRatingsByVetId(VetsEntityDtoUtil.verifyId(vetId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "vets/{vetId}/ratings")
    public Mono<RatingResponseDTO> addRatingToVet(@PathVariable String vetId, @RequestBody Mono<RatingRequestDTO> ratingRequestDTO) {
        return vetsServiceClient.addRatingToVet(vetId, ratingRequestDTO);
    }

    @DeleteMapping(value = "vets/{vetId}/ratings/{ratingId}")
    public Mono<Void> deleteRatingByRatingId(@PathVariable String vetId,
                                             @PathVariable String ratingId){
        return vetsServiceClient.deleteRating(vetId,ratingId);
    }

    @GetMapping(value = "vets/{vetId}/ratings/average")
    public Mono<ResponseEntity<Double>> getAverageRatingByVetId(@PathVariable String vetId){
        return vetsServiceClient.getAverageRatingByVetId(VetsEntityDtoUtil.verifyId(vetId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
  
    @GetMapping("/vets/{vetId}")
    public Mono<ResponseEntity<VetDTO>> getVetByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getVetByVetId(VetsEntityDtoUtil.verifyId(vetId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("/vets/vetBillId/{vetId}")
    public Mono<ResponseEntity<VetDTO>> getVetByVetBillId(@PathVariable String vetBillId) {
        return vetsServiceClient.getVetByVetBillId(VetsEntityDtoUtil.verifyId(vetBillId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("/vets/active")
    public Flux<VetDTO> getActiveVets() {
        return vetsServiceClient.getActiveVets();
    }

    @GetMapping("/vets/inactive")
    public Flux<VetDTO> getInactiveVets() {
        return vetsServiceClient.getInactiveVets();
    }

    @PostMapping(value = "/vets",consumes = "application/json",produces = "application/json")
    public Mono<VetDTO> insertVet(@RequestBody Mono<VetDTO> vetDTOMono) {
        return vetsServiceClient.createVet(vetDTOMono);
    }

    @PutMapping(value = "/vets/{vetId}",consumes = "application/json",produces = "application/json")
    public Mono<ResponseEntity<VetDTO>> updateVetByVetId(@PathVariable String vetId, @RequestBody Mono<VetDTO> vetDTOMono) {
        return vetsServiceClient.updateVet(VetsEntityDtoUtil.verifyId(vetId), vetDTOMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/vets/{vetId}")
    public Mono<Void> deleteVet(@PathVariable String vetId) {
        return vetsServiceClient.deleteVet(VetsEntityDtoUtil.verifyId(vetId));
    }

//    @PostMapping(value = "users",
//            consumes = "application/json",
//            produces = "application/json")
//    public Mono<UserDetails> createUser(@RequestBody Register model) {
//        return authServiceClient.createUser(model);
//    }
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
//    @GetMapping(value = "users")
//    public Flux<UserDetails> getAll(@RequestHeader(AUTHORIZATION) String auth) {
//        return authServiceClient.getUsers(auth);
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
     * **/

    @GetMapping(value = "owners")
    public Flux<OwnerResponseDTO> getAllOwners() {
        return customersServiceClient.getAllOwners();
                /*.flatMap(n ->
                        visitsServiceClient.getVisitsForPets(n.getPetIds())
                                .map(addVisitsToOwner(n))
                );*/
    }

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

    @PostMapping(value = "owners",
            consumes = "application/json",
            produces = "application/json")
    public Mono<OwnerResponseDTO> createOwner(@RequestBody OwnerResponseDTO model){
        return customersServiceClient.createOwner(model);
    }

    @PostMapping(value = "owners/photo/{ownerId}")
    public Mono<String> setOwnerPhoto(@RequestBody PhotoDetails photoDetails, @PathVariable int ownerId) {
        return customersServiceClient.setOwnerPhoto(photoDetails, ownerId);
    }

    /*@GetMapping(value = "owners/photo/{ownerId}")
    public Mono<PhotoDetails> getOwnerPhoto(@PathVariable int ownerId) {
        return customersServiceClient.getOwnerPhoto(ownerId);
    }*/

    @PostMapping(value = "owners/{ownerId}/pet/photo/{petId}")
    public Mono<String> setPetPhoto(@PathVariable String ownerId, @RequestBody PhotoDetails photoDetails, @PathVariable int petId) {
        return customersServiceClient.setPetPhoto(ownerId, photoDetails, petId);
    }

    @GetMapping(value = "owners/{ownerId}/pet/photo/{petId}")
    public Mono<PhotoDetails> getPetPhoto(@PathVariable String ownerId, @PathVariable int petId) {
        return customersServiceClient.getPetPhoto(ownerId, petId);
    }

    @DeleteMapping(value = "owners/photo/{photoId}")
    public Mono<Void> deleteOwnerPhoto(@PathVariable int photoId){
        return customersServiceClient.deleteOwnerPhoto(photoId);
    }

    @DeleteMapping(value = "owners/{ownerId}/pet/photo/{photoId}")
    public Mono<Void> deletePetPhoto(@PathVariable int ownerId, @PathVariable int photoId){
        return customersServiceClient.deletePetPhoto(ownerId, photoId);
    }

    @PutMapping(value = "owners/{ownerId}",consumes = "application/json" ,produces = "application/json")
    public Mono<OwnerResponseDTO> updateOwnerDetails(@PathVariable int ownerId, @RequestBody OwnerResponseDTO od) {
        return customersServiceClient.updateOwner(ownerId, od);
                /*.flatMap(owner ->
                        visitsServiceClient.getVisitsForPets(owner.getPetIds())
                                .map(addVisitsToOwner(owner)));*/
    }








    @DeleteMapping(value = "owners/{ownerId}")
    public Mono<OwnerResponseDTO> deleteOwner(@PathVariable int ownerId){
        return customersServiceClient.deleteOwner(ownerId);
    }
    
    /**
     * End of Owner Methods
     **/

//    @GetMapping("/verification/{token}")
//    public Mono<UserDetails> verifyUser(@PathVariable final String token) {
//        return authServiceClient.verifyUser(token);
//    }




    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "/users/login",produces = "application/json;charset=utf-8;", consumes = "application/json")
    public Mono<ResponseEntity<UserPasswordLessDTO>> login(@RequestBody Login login) throws Exception {
        log.info("Entered controller /login");
        log.info("Login: " + login.getEmail() + " " + login.getPassword());
        HttpEntity<UserPasswordLessDTO> responseFromService = authServiceClient.login(login);


        return Mono.just(ResponseEntity.status(HttpStatus.OK).headers(responseFromService.getHeaders()).body(responseFromService.getBody()));

    }

    //Start of Inventory Methods
    @PostMapping(value = "inventory/{inventoryId}/products")
    public Mono<ProductResponseDTO> addProductToInventory(@RequestBody ProductRequestDTO model, @PathVariable String inventoryId){
        return inventoryServiceClient.addProductToInventory(model, inventoryId);
    }


    @PostMapping(value = "inventory")
    public Mono<ResponseEntity<InventoryResponseDTO>> addInventory(@RequestBody InventoryRequestDTO model){
        return inventoryServiceClient.addInventory(model)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    }


    @PutMapping(value = "inventory/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> updateInventory( @RequestBody InventoryRequestDTO model, @PathVariable String inventoryId) {
        return inventoryServiceClient.updateInventory(model, inventoryId)
                .map(updatedStudent -> ResponseEntity.status(HttpStatus.OK).body(updatedStudent))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }



    @DeleteMapping(value = "inventory/{inventoryId}/products/{productId}")
    public Mono<Void> deleteProductInInventory(@PathVariable String inventoryId, @PathVariable String productId){
        return inventoryServiceClient.deleteProductInInventory(inventoryId, productId);
    }
}