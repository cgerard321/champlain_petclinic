package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface OwnerService {

    Mono<Optional<Owner>> findByOwnerId(int id);
    Flux<Owner> findAll();
    Mono<Owner> updateOwner(int id, Mono<Owner> OwnerMono);
    Mono<Owner> createOwner(Mono<Owner> OwnerMono);
    Mono<Void> deleteOwner(int id);

}
