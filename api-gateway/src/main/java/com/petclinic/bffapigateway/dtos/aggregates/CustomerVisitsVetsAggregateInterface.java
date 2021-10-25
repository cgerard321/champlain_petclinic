package com.petclinic.bffapigateway.dtos.aggregates;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface CustomerVisitsVetsAggregateInterface {

    @GetMapping(
            value = "/api/gateway/aggregate/{customerId}",
            produces = "application/json")
    Mono<CustomerVisitsVetsAggregate> getCustomer(@PathVariable int customerId);
}