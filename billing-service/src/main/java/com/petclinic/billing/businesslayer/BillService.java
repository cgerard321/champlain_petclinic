package com.petclinic.billing.businesslayer;


import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.BillResponseDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BillService {
    Mono<BillResponseDTO> GetBill(@RequestParam(value = "billId", required = true) String billId);
    Flux<BillResponseDTO> GetAllBills();

    Mono<BillResponseDTO> CreateBill(@RequestBody Mono<BillRequestDTO> model);

    Mono<Void> DeleteBill(@RequestParam(value = "billId", required = true) String billId);

    Flux<BillResponseDTO> GetBillsByCustomerId(@RequestParam(value = "customerId", required = true) int customerId);
    Flux<BillResponseDTO> GetBillsByVetId(@RequestParam(value = "vetId", required = true) String vetId);

    Flux<Void> DeleteBillsByVetId(@RequestParam(value="vetId", required = true) String vetId);
    Flux<Void> DeleteBillsByCustomerId(@RequestParam(value="customerId", required = true)int customerId);

    Mono<BillDTO> updateBill(String billId, Mono<BillDTO> billDTOMono);

}
