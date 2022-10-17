package com.petclinic.customersservice.business;

import reactor.core.publisher.Mono;

public interface OwnerDTOService {

    Mono<OwnerDTO> getOwnerAggregateByOwnerId(int ownerId);

}
