package com.petclinic.bffapigateway.presentationlayer;


import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.*;
import com.petclinic.bffapigateway.utils.VetsEntityDtoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

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

    @GetMapping(value = "bills/{billId}")
    public Mono<BillDetails> getBillingInfo(final @PathVariable String billId)
    {
        return billServiceClient.getBilling(billId);
    }

    @PostMapping(value = "bills",
            consumes = "application/json",
            produces = "application/json")
    public Mono<BillDetails> createBill(@RequestBody BillDetails model) {
        return billServiceClient.createBill(model);
    }

    @GetMapping(value = "bills")
    public Flux<BillDetails> getAllBilling() {
        return billServiceClient.getAllBilling();
    }

    @GetMapping(value = "bills/customer/{customerId}")
    public Flux<BillDetails> getBillsByOwnerId(final @PathVariable int customerId)
    {
        return billServiceClient.getBillsByOwnerId(customerId);
    }

    @GetMapping(value = "bills/vet/{vetId}")
    public Flux<BillDetails> getBillsByVetId(final @PathVariable String vetId)
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



    @PostMapping(value = "owners/{ownerId}/pets" , produces = "application/json", consumes = "application/json")
    public Mono<PetDetails> createPet(@RequestBody PetDetails pet, @PathVariable int ownerId){
        return customersServiceClient.createPet(pet, ownerId);
    }

    @GetMapping(value = "owners/{ownerId}/pets/{petId}")
    public Mono<PetDetails> getPet(@PathVariable int ownerId, @PathVariable int petId){
        return customersServiceClient.getPet(ownerId, petId);
    }

    @DeleteMapping("owners/{ownerId}/pets/{petId}")
    public Mono<PetDetails> deletePet(@PathVariable int ownerId, @PathVariable int petId){
        return customersServiceClient.deletePet(ownerId,petId);
    }

    @GetMapping("owners/petTypes")
    public Flux<PetType> getPetTypes(){
        return customersServiceClient.getPetTypes();
    }



    @PutMapping(
            value = "owners/*/pets/{petId}/visits/{visitId}",
            consumes = "application/json",
            produces = "application/json"
    )
    Mono<VisitDetails> updateVisit(@RequestBody VisitDetails visit, @PathVariable int petId, @PathVariable String visitId) {
        visit.setPetId(petId);
        visit.setVisitId(visitId);
        return visitsServiceClient.updateVisitForPet(visit);
    }

    @DeleteMapping (value = "visits/{visitId}")
    public Mono<Void> deleteVisitsByVisitId(final @PathVariable String visitId){
        return visitsServiceClient.deleteVisitByVisitId(visitId);
    }

    @GetMapping(value = "visits/{petId}")
    public Flux<VisitDetails> getVisitsForPet(final @PathVariable int petId){
        return visitsServiceClient.getVisitsForPet(petId);
    }
    
    @GetMapping(value ="visit/{visitId}")
    public Mono<VisitDetails> getVisitByVisitId(final @PathVariable String visitId){
        return visitsServiceClient.getVisitByVisitId(visitId);
    }
    
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

    @GetMapping(value = "visits/calendar/{practitionerId}")
    public Flux<VisitDetails> getVisitsByPractitionerIdAndMonth(@PathVariable("practitionerId") int practitionerId,
                                                                @RequestParam("dates") List<String> dates) {
        String startDate = dates.get(0);
        String endDate = dates.get(1);

        return visitsServiceClient.getVisitsByPractitionerIdAndMonth(practitionerId, startDate, endDate);
    }

    private Function<Visits, OwnerDetails> addVisitsToOwner(OwnerDetails owner) {
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

    @PostMapping(
            value = "visit/owners/{ownerId}/pets/{petId}/visits",
            consumes = "application/json",
            produces = "application/json"
    )
    Mono<VisitDetails> addVisit(@RequestBody VisitDetails visit, @PathVariable String ownerId, @PathVariable String petId) {
        visit.setPetId(Integer.parseInt(petId));
        return visitsServiceClient.createVisitForPet(visit);
    }




    @GetMapping(value = "vets")
    public Flux<VetDTO> getAllVets() {
        return vetsServiceClient.getVets();
    }

    @GetMapping("/vets/{vetId}")
    public Mono<ResponseEntity<VetDTO>> getVetByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getVetByVetId(VetsEntityDtoUtil.verifyId(vetId))
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



    @PostMapping(value = "users",
            consumes = "application/json",
            produces = "application/json")
    public Mono<UserDetails> createUser(@RequestBody Register model) {
        return authServiceClient.createUser(model);
    }

    @DeleteMapping(value = "users/{userId}")
    public Mono<UserDetails> deleteUser(final @PathVariable long userId) {
        return authServiceClient.deleteUser(userId);
    }

    @GetMapping(value = "users/{userId}")
    public Mono<UserDetails> getUserDetails(final @PathVariable long userId) {
        return authServiceClient.getUser(userId);
    }
    @GetMapping(value = "users")
    public Flux<UserDetails> getAll() {
        return authServiceClient.getUsers();
    }

    @PutMapping(value = "users/{userId}",
            consumes = "application/json",
            produces = "application/json")
    public Mono<UserDetails> updateUser(final @PathVariable long userId, @RequestBody Register model) {
        return authServiceClient.updateUser(userId, model);
    }

    @GetMapping(value = "admin/roles")
    public Flux<Role> getRoles() {
        return authServiceClient.getRoles();
    }

    @DeleteMapping(value = "admin/roles/{id}")
    public Mono<Void> deleteRole(@PathVariable int id) {
        return authServiceClient.deleteRole(id);
    }

    @PostMapping(value = "admin/roles")
    public Mono<Role> addRole(@RequestBody final Role model) {
        return authServiceClient.addRole(model);
    }


    /**
     * Owners Methods
     * **/

    @GetMapping(value = "owners")
    public Flux<OwnerDetails> getOwners() {
        return customersServiceClient.getOwners()
                .flatMap(n ->
                        visitsServiceClient.getVisitsForPets(n.getPetIds())
                                .map(addVisitsToOwner(n))
                );
    }

    @GetMapping(value = "owners/{ownerId}")
    public Mono<OwnerDetails> getOwnerDetails(final @PathVariable int ownerId) {
        return customersServiceClient.getOwner(ownerId)
                .flatMap(owner ->
                        visitsServiceClient.getVisitsForPets(owner.getPetIds())
                                .map(addVisitsToOwner(owner))
                );
    }

    @PostMapping(value = "owners",
            consumes = "application/json",
            produces = "application/json")
    public Mono<OwnerDetails> createOwner(@RequestBody OwnerDetails model){
        return customersServiceClient.createOwner(model);
    }


    @PostMapping(value = "owners/photo/{ownerId}")
    public Mono<String> setOwnerPhoto(@RequestBody PhotoDetails photoDetails, @PathVariable int ownerId) {
        return customersServiceClient.setOwnerPhoto(photoDetails, ownerId);
    }

    @GetMapping(value = "owners/photo/{ownerId}")
    public Mono<PhotoDetails> getOwnerPhoto(@PathVariable int ownerId) {
        return customersServiceClient.getOwnerPhoto(ownerId);
    }

    @PostMapping(value = "owners/{ownerId}/pet/photo/{petId}")
    public Mono<String> setPetPhoto(@PathVariable int ownerId, @RequestBody PhotoDetails photoDetails, @PathVariable int petId) {
        return customersServiceClient.setPetPhoto(ownerId, photoDetails, petId);
    }

    @GetMapping(value = "owners/{ownerId}/pet/photo/{petId}")
    public Mono<PhotoDetails> getPetPhoto(@PathVariable int ownerId, @PathVariable int petId) {
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
    public Mono<OwnerDetails> updateOwnerDetails(@PathVariable int ownerId, @RequestBody OwnerDetails od) {
        return customersServiceClient.updateOwner(ownerId, od)
                .flatMap(owner ->
                        visitsServiceClient.getVisitsForPets(owner.getPetIds())
                                .map(addVisitsToOwner(owner)));
    }

    @DeleteMapping(value = "owners/{ownerId}")
    public Mono<OwnerDetails> deleteOwner(@PathVariable int ownerId){
        return customersServiceClient.deleteOwner(ownerId);
    }
    
    /**
     * End of Owner Methods
     * **/


    @GetMapping("/verification/{token}")
    public Mono<UserDetails> verifyUser(@PathVariable final String token) {
        return authServiceClient.verifyUser(token);
    }

    @PostMapping("/users/login")
    public Mono<ResponseEntity<UserDetails>> login(@RequestBody final Login login) {
        return authServiceClient.login(login)
                .map(n -> ResponseEntity.ok()
                        .header(AUTHORIZATION, n.getT1())
                        .body(n.getT2())
                );
    }

    //Start of Bundle Methods

    @GetMapping(value = "bundles/{bundleUUID}")
    public Mono<BundleDetails> getBundle(final @PathVariable String bundleUUID)
    {
        return inventoryServiceClient.getBundle(bundleUUID);
    }
    @GetMapping(value = "bundles")
    public Flux<BundleDetails> getAllBundles() {
        return inventoryServiceClient.getAllBundles();
    }
    @GetMapping(value = "bundles/item/{item}")
    public Flux<BundleDetails> getBundlesByItem(@PathVariable String item) {
        return inventoryServiceClient.getBundlesByItem(item);
    }
    @PostMapping(value = "bundles",
            consumes = "application/json",
            produces = "application/json")
    public Mono<BundleDetails> createBundle(@RequestBody BundleDetails model) {
        return inventoryServiceClient.createBundle(model);
    }
    @DeleteMapping(value = "bundles/{bundleUUID}")
    public Mono<Void> deleteBundle(final @PathVariable String bundleUUID){
        return inventoryServiceClient.deleteBundle(bundleUUID);
    }

}