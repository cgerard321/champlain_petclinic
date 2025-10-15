package com.petclinic.billing.datalayer;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    Flux<Bill> findByCustomerIdAndBillStatus(String customerId, BillStatus status);

    @Query("{ 'date' : { $gte: ?0, $lt: ?1 } }")
    Flux<Bill> findByDateBetween(LocalDate start, LocalDate end);

    Mono<Bill> findByCustomerIdAndBillId(String customerId, String billId);

    Flux<Bill> findAllByArchiveFalse();

    Flux<Bill> findAllByDateBefore(LocalDate date);

    Flux<Bill> findByCustomerIdAndAmountBetween(String customerId, BigDecimal minAmount, BigDecimal maxAmount);

    Flux<Bill> findByCustomerIdAndDueDateBetween(String customerId, LocalDate startDate, LocalDate endDate);

    Flux<Bill> findByCustomerIdAndDateBetween(String customerId, LocalDate startDate, LocalDate endDate);

}