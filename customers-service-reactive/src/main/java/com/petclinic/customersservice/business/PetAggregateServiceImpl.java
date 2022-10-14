package com.petclinic.customersservice.business;

import com.petclinic.customersservice.util.EntityAggregateUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PetAggregateServiceImpl implements PetAggregateService {

    @Override
    public Mono<PetAggregate> insertPetAggregate(Mono<PetAggregate> petAggregateMono) {
        return petAggregateMono
                .map(EntityAggregateUtil::toPet)
                .
    }
}
