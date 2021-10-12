package com.petclinic.billing.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Integer> {
    @Transactional(readOnly = true)
    List<Bill> findByBillId(int billId);

    @Transactional(readOnly = true)
    List<Bill> findByCustomerId(int customerId);
}
