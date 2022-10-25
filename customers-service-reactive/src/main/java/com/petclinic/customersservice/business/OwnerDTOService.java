package com.petclinic.customersservice.business;

import reactor.core.publisher.Mono;

public interface OwnerDTOService {

    Mono<OwnerDTO> getOwnerDTOByOwnerId(String ownerId);

}
