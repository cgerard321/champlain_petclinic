package com.petclinic.billing.datalayer;

//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    Flux<Bill> findByCustomerId(String customerId);

    @Transactional(readOnly = true)
    Flux<Bill> findByVetId(String vetId);

    Mono<Void>deleteBillByBillId(String billId);

    Flux<Void> deleteBillsByVetId(String vetId);
    Flux<Void> deleteBillsByCustomerId(String customerId);

    Flux<Bill> findAllBy(Pageable pageable);

    @Transactional(readOnly = true)
    Flux<Bill> findAllBillsByBillStatus(BillStatus status);


}

}