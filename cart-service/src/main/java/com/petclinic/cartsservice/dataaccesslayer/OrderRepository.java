package com.petclinic.cartsservice.dataaccesslayer;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    public Mono<Order> findOrderByOrderId(String orderId);
    public Flux<Order> findAllByCustomerId(String customerId);
    public Mono<Boolean> existsByStripeSessionId(String stripeSessionId);
}
