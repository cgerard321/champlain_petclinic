package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import reactor.core.publisher.Mono;

public interface OwnerAggregateService {

    Mono<OwnerAggregate> insertOwnerAggregate(OwnerAggregate ownerAggregateMono);

}
