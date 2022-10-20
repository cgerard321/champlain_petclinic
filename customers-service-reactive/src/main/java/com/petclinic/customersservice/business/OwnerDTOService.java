package com.petclinic.customersservice.business;

import reactor.core.publisher.Mono;

public interface OwnerDTOService {

    public Mono<OwnerDTO> getOwnerDTOByOwnerId(int ownerId);

}
