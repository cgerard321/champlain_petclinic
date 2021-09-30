package com.petclinic.bffapigateway.presentationlayer;


import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Login;
import com.petclinic.bffapigateway.dtos.OwnerDetails;
import com.petclinic.bffapigateway.dtos.VetDetails;
import com.petclinic.bffapigateway.dtos.Visits;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.petclinic.bffapigateway.dtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
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

    @GetMapping(value = "owners/{ownerId}")
    public Mono<OwnerDetails> getOwnerDetails(final @PathVariable int ownerId) {
        return customersServiceClient.getOwner(ownerId)
                .flatMap(owner ->
                        visitsServiceClient.getVisitsForPets(owner.getPetIds())
                                .map(addVisitsToOwner(owner))
                );
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
    public void deleteVisitsById(final @PathVariable int visitId){
        visitsServiceClient.deleteVisitsById(visitId);
    }

    //Delete Visit
    @DeleteMapping (value = "pets/visits/{petId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Void> deleteVisitForPets(final @PathVariable int petId){
        return visitsServiceClient.deleteVisitForPets(petId);
    }

    //Update Visit
    @PostMapping(value ="pets/visits/{petId}", consumes = "application/json", produces = "application/json")
    public Mono<Visits> updateVisitForPets(final @PathVariable int petId){
        return visitsServiceClient.updateVisitForPets(petId);
    }

    @GetMapping(value = "visits/{petId}")
    public Flux<VisitDetails> getVisitsForPet(@PathVariable int petId){
        return visitsServiceClient.getVisitsForPet(petId);
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

    @GetMapping(value = "vets")
    public Flux<VetDetails> getVets() {
        return vetsServiceClient.getVets();
    }


    @PostMapping(value = "users",
            consumes = "application/json",
            produces = "application/json")
    public Mono<UserDetails> createUser(@RequestBody UserDetails model) { return authServiceClient.getUser(model.getId()); }

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

}
