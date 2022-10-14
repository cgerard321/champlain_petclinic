package com.petclinic.customersservice.business;

import reactor.core.publisher.Mono;

public interface PetAggregateService {

    Mono<PetAggregate> insertPetAggregate(Mono<PetAggregate> petAggregateMono);

}
