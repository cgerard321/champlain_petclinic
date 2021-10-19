package com.petclinic.bffapigateway.presentationlayer;


import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Maciej Szarlinski
 * @author Christine Gerard
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 * Modified to remove circuitbreaker
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gateway")
public class BFFApiGatewayController {

    private final CustomersServiceClient customersServiceClient;

    private final VisitsServiceClient visitsServiceClient;

    private final VetsServiceClient vetsServiceClient;


    private final AuthServiceClient authServiceClient;

    private final BillServiceClient billServiceClient;



    @GetMapping(value = "owners/{ownerId}")
    public Mono<OwnerDetails> getOwnerDetails(final @PathVariable int ownerId) {
        return customersServiceClient.getOwner(ownerId)
                .flatMap(owner ->
                        visitsServiceClient.getVisitsForPets(owner.getPetIds())
                                .map(addVisitsToOwner(owner))
                );
    }
//check this
    @GetMapping(value = "bills/{billId}")
    public Mono<BillDetails> getBillingInfo(final @PathVariable int billId)
    {
        return billServiceClient.getBilling(billId);
    }



    @GetMapping(value = "customer/owners")
    public Flux<OwnerDetails> getOwners() {
        return customersServiceClient.getOwners()
                .flatMap(n ->
                        visitsServiceClient.getVisitsForPets(n.getPetIds())
                                .map(addVisitsToOwner(n))
                );
    }
    //Testing purpose
    @GetMapping(value = "pets/visits/All")
    public Mono<Visits> getAllVisits(){
        return visitsServiceClient.getAllVisits();

    }


/*
    //Add new Visit
    @PostMapping (value = "/pets/visits", consumes = "application/json", produces = "application/json")
    public Mono<Visits> createVisitForPets(final @RequestBody VisitDetails visitDetails){
        return visitsServiceClient.createVisitForPets(visitDetails);
        }
*/

    @PutMapping(
            value = "owners/*/pets/{petId}/visits/{id}",
            consumes = "application/json",
            produces = "application/json"
    )
    Mono<VisitDetails> updateVisit(@RequestBody VisitDetails visit, @PathVariable int petId, @PathVariable int id) {
        visit.setPetId(petId);
        visit.setId(id);
        return visitsServiceClient.updateVisitForPet(visit);
    }

    @DeleteMapping (value = "visits/{visitId}")
    public Mono<Void> deleteVisitsById(final @PathVariable int visitId){
        return visitsServiceClient.deleteVisitsById(visitId);
    }

    //Delete Visit
    @DeleteMapping (value = "pets/visits/{petId}")
    public Mono<Void> deleteVisitForPets(final @PathVariable int petId){
        return visitsServiceClient.deleteVisitForPets(petId);
    }

    //Update Visit
    @PutMapping(value ="pets/visits/{petId}", consumes = "application/json", produces = "application/json")
    public Mono<Visits> updateVisitForPets(final @PathVariable int petId){
        return visitsServiceClient.updateVisitForPets(petId);
    }

    @GetMapping(value = "visits/{petId}")
    public Flux<VisitDetails> getVisitsForPet(final @PathVariable int petId){
        return visitsServiceClient.getVisitsForPet(petId);
    }

    @GetMapping(value = "visits/vets/{practitionerId}")
    public Flux<VisitDetails> getVisitForPractitioner(@PathVariable int practitionerId){
        return visitsServiceClient.getVisitForPractitioner(practitionerId);
    }

    @PutMapping(value = "owners/{ownerId}",consumes = "application/json" ,produces = "application/json")
    public Mono<OwnerDetails> updateOwnerDetails(@RequestBody OwnerDetails od, final @PathVariable int ownerId) {


        return customersServiceClient.updateOwner(od,ownerId)
                .flatMap(owner ->
                        visitsServiceClient.getVisitsForPets(owner.getPetIds())
                                .map(addVisitsToOwner(owner)));





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

    /**
     * Retrieve all vets from DB
     */
    @GetMapping(value = "vets")
    public Flux<VetDetails> getVets() {
        return vetsServiceClient.getVets();
    }

    /**
     * Get a single vet given its vetID
     */
    @GetMapping(value = "vets/{vetId}")
    public Mono<VetDetails> getVet(final @PathVariable long vetId) {
        return vetsServiceClient.getVet(vetId);
    }

    /**
     * Create Vet
     */
    @PostMapping(value = "vets",
            consumes = "application/json",
            produces = "application/json")
    public Mono<VetDetails> createVet(@RequestBody VetDetails model) { return vetsServiceClient.getVet(model.getVetId()); }

    /**
     * Delete vet from DB given the vetID
     */
    @DeleteMapping(value = "vets/{vetId}")
    public Mono<VetDetails> deleteVet(final @PathVariable long vetId) {
        return vetsServiceClient.deleteVet(vetId);
    }

    /**
     * Update vet details
     */
    @PutMapping(
            value = "vets/{vetId}",
            consumes = "application/json",
            produces = "application/json"
    )
    Mono<VetDetails> updateVet(@RequestBody VetDetails vet, @PathVariable int vetId) {
        return vetsServiceClient.updateVet(vetId, vet);
    }

    @PostMapping(value = "users",
            consumes = "application/json",
            produces = "application/json")
    public Mono<UserDetails> createUser(@RequestBody Register model) {
        return authServiceClient.createUser(model);
    }

    @DeleteMapping(value = "users/{userId}")
    public Mono<UserDetails> deleteUser(final @PathVariable long userId) { return authServiceClient.deleteUser(userId); }

    @GetMapping(value = "users/{userId}")
    public Mono<UserDetails> getUserDetails(final @PathVariable long userId) {
        return authServiceClient.getUser(userId);
    }


    // TODO: Hook this up to auth service
    @GetMapping(value = "admin/roles")
    public Object getRoles() {
        return null;
    }

    // TODO: Hook this up to auth service
    @DeleteMapping(value = "admin/roles/{id}")
    public void deleteRole(@PathVariable int id) {

    }

    // TODO: Hook this up to auth service
    @PostMapping(value = "admin/roles")
    public Object addRole() {
        return null;
    }

    @PostMapping(value = "owners",
            consumes = "application/json",
            produces = "application/json")
    public Mono<OwnerDetails> createOwner(@RequestBody OwnerDetails model){ return customersServiceClient.getOwner(model.getId()); }

    @GetMapping("/verification/{token}")
    public Mono<UserDetails> verifyUser(@PathVariable final String token) {
        return authServiceClient.verifyUser(token);
    }
}