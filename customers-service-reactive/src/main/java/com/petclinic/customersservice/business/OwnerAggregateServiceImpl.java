package com.petclinic.customersservice.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OwnerAggregateServiceImpl implements OwnerAggregateService {

    @Override
    public Mono<OwnerAggregate> insertOwnerAggregate(OwnerAggregate ownerAggregateMono) {
        return null;
    }
}
