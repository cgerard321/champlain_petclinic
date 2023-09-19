package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OwnerService {
    Flux<OwnerResponseDTO> getAllOwners();

    Mono<Owner> insertOwner(Mono<Owner> ownerMono);

    // getOwnerByOwnerId is now returning a OwnerResponseDTO
    Mono<OwnerResponseDTO> getOwnerByOwnerId(String ownerId);

    Mono<Void> deleteOwner(String ownerId);

    Mono<Owner> updateOwner(String ownerId, Mono<Owner> ownerMono);


}
