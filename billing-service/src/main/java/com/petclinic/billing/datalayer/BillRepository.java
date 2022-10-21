package com.petclinic.billing.datalayer;

//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BillRepository extends ReactiveMongoRepository<Bill, String> {

    @Transactional(readOnly = true)
    Mono<Bill> findByBillId(String billId);

    @Transactional(readOnly = true)
    Flux<Bill> findByCustomerId(int customerId);

    Mono<Void>deleteBillByBillId(String billId);

    @Transactional(readOnly = true)
    Flux<Bill> findByVetId(String vetBillId);

    Flux<Void> deleteBillsByCustomerId(int customerId);

    Flux<Void> deleteBillsByVetId (String vetId);
}