package com.petclinic.cartsservice.dataaccesslayer;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    Mono<Order> findOrderByOrderId(String orderId);

    Mono<Order> findByOrderIdAndCustomerId(String orderId, String customerId);

    Flux<Order> findAllByCustomerId(String customerId);
}
