package com.petclinic.bffapigateway.presentationlayer;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;

import com.petclinic.bffapigateway.dtos.*;

import com.petclinic.bffapigateway.dtos.Login;
import com.petclinic.bffapigateway.dtos.OwnerDetails;
import com.petclinic.bffapigateway.dtos.VetDetails;
import com.petclinic.bffapigateway.dtos.VisitDetails;
import com.petclinic.bffapigateway.dtos.Visits;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    private final AuthServiceClient authenticationServiceClient;

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
    
    @GetMapping(value = "vets")
    public Flux<VetDetails> getVets() {
        return vetsServiceClient.getVets();
    }

    @GetMapping(value = "vets/disabled")
    public Flux<VetDetails> getDisabledVets() {
        return vetsServiceClient.getDisabledVets();
    }

    @GetMapping(value = "vets/{vetId}")
    public Mono<VetDetails> getVetDetails(final @PathVariable int vetId) {
        return vetsServiceClient.getVet(vetId);
    }

    // TODO: Hook this up to auth service
    @GetMapping(value = "/admin/roles")
    public Object getRoles() {
        return null;
    }

    // TODO: Hook this up to auth service
    @DeleteMapping(value = "/admin/roles/{id}")
    public void deleteRole(@PathVariable int id) {

    }

    // TODO: Hook this up to auth service
    @PostMapping(value = "/admin/roles")
    public Object addRole() {
        return null;
    }

    @PostMapping(value = "/owners",
            consumes = "application/json",
            produces = "application/json")
    public Mono<OwnerDetails> createOwner(@RequestBody OwnerDetails model){ return customersServiceClient.getOwner(model.getId()); }




    @GetMapping(value = "users/{userId}")
    public Mono<UserDetails> getUserDetails(final @PathVariable int userId) {
        return authenticationServiceClient.getUser(userId);
    }

    @PostMapping(value = "/users",
            consumes = "application/json",
            produces = "application/json")
    public Mono<UserDetails> createUser(@RequestBody UserDetails model){ return authenticationServiceClient.getUser(model.getId()); }

}
