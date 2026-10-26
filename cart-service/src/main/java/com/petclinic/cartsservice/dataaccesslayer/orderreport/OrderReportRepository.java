package com.petclinic.cartsservice.dataaccesslayer.orderreport;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface OrderReportRepository
        extends ReactiveMongoRepository<OrderReport, String> {

    Mono<Boolean> existsByOrderIdAndStatusIn(
            String orderId,
            Collection<OrderReportStatus> statuses
    );

    Flux<OrderReport> findAllByCustomerIdOrderBySubmittedAtDesc(
            String customerId
    );
}
