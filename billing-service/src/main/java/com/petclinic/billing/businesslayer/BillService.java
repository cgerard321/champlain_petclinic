package com.petclinic.billing.businesslayer;


import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BillService {
    Mono<BillResponseDTO> getBillByBillId(String billId);

<<<<<<< HEAD
    Flux<BillResponseDTO> GetAllBillsByStatus(BillStatus status);


    Flux<BillResponseDTO> GetAllBills();
=======
    Flux<BillResponseDTO> GetAllBills(int page, int size);
>>>>>>> 6b04c66b (Implementation of pagination - backend)

    Mono<BillResponseDTO> CreateBill(@RequestBody Mono<BillRequestDTO> model);

    Mono<Void> DeleteBill(@RequestParam(value = "billId", required = true) String billId);

    Flux<BillResponseDTO> GetBillsByCustomerId(@RequestParam(value = "customerId", required = true) String customerId);
    Flux<BillResponseDTO> GetBillsByVetId(@RequestParam(value = "vetId", required = true) String vetId);

    Flux<Void> DeleteBillsByVetId(@RequestParam(value="vetId", required = true) String vetId);
    Flux<Void> DeleteBillsByCustomerId(@RequestParam(value="customerId", required = true)String customerId);

    Mono<BillResponseDTO> updateBill(String billId, Mono<BillRequestDTO> billRequestDTO);

    Mono<Void> DeleteAllBills();

}
