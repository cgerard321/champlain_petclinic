package com.petclinic.billing.businesslayer;


import com.petclinic.billing.datalayer.BillDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BillService {
    Mono<BillDTO> GetBill(@RequestParam(value = "billId", required = true) String billId);
    Flux<BillDTO> GetAllBills();

    Mono<BillDTO> CreateBill(@RequestBody Mono<BillDTO> model);

    Mono<Void> DeleteBill(@RequestParam(value = "billId", required = true) String billId);

    Flux<BillDTO> GetBillsByCustomerId(@RequestParam(value = "customerId", required = true) int customerId);


}
