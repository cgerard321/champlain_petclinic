package com.petclinic.billing.datalayer;

//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BillRepository extends ReactiveCrudRepository<Bill, Integer> {
    @Transactional(readOnly = true)

    Mono<Bill> findByBillId(int bill_id);

    @Transactional(readOnly = true)
    Flux<Bill> findByCustomerId(int customer_id);
}
