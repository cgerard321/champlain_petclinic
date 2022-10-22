package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import reactor.core.publisher.Mono;

public interface OwnerService {

    Mono<Owner> insertOwner(Mono<Owner> ownerMono);

    Mono<Owner> getOwnerByOwnerId(String ownerId);

    Mono<Void> deleteOwner(String Id);

}
