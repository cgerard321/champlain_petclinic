package com.petclinic.billing.businesslayer;


import com.petclinic.billing.datalayer.BillDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BillService {
    Mono<BillDTO> GetBill(String billId);
    Flux<BillDTO> GetAllBills();

    Mono<BillDTO> CreateBill(Mono<BillDTO> model);

    Mono<Void> DeleteBill(String billId);

    Flux<BillDTO> GetBillsByOwnerId(String ownerId);
    Flux<BillDTO> GetBillsByVetId(String vetId);

    Flux<Void> DeleteBillsByVetId(String vetId);
    Flux<Void> DeleteBillsByOwnerId(String ownerId);

    Mono<BillDTO> updateBill(String billId, Mono<BillDTO> billDTOMono);

}
