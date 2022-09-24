package com.petclinic.billing.businesslayer;


import com.petclinic.billing.datalayer.BillDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BillService {
    Mono<BillDTO> GetBill(@RequestParam(value = "billId", required = true) int billId);

    Flux<BillDTO> GetAllBills();

    Mono<BillDTO> CreateBill(@RequestBody BillDTO model);

    Mono<Void> DeleteBill(@RequestParam(value = "billId", required = true) int billId);

    Flux<BillDTO> GetBillByCustomerId(@RequestParam(value = "customerId", required = true) int customerId);
}
