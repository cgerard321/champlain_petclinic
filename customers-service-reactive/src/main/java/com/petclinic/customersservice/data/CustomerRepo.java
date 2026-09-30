package com.petclinic.customersservice.data;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepo extends ReactiveMongoRepository<Customer, String> {

    Mono<Customer> findCustomerByCustomerId(String customerId);

    Mono<Void> deleteById(String Id);

}
