package com.petclinic.billing.businesslayer;


import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BillService {
    Mono<BillResponseDTO> getBillByBillId(String billId);

    Flux<BillResponseDTO> GetAllBillsByStatus(BillStatus status);


    Flux<BillResponseDTO> GetAllBills();

    //to be changed
    Flux<BillResponseDTO> getAllBillsByPage(Pageable pageable);
    //to be changed
    Mono<Long> getNumberOfBills();

    Mono<BillResponseDTO> CreateBill(@RequestBody Mono<BillRequestDTO> model);

    Mono<Void> DeleteBill(@RequestParam(value = "billId", required = true) String billId);

    Flux<BillResponseDTO> GetBillsByCustomerId(@RequestParam(value = "customerId", required = true) String customerId);
    Flux<BillResponseDTO> GetBillsByVetId(@RequestParam(value = "vetId", required = true) String vetId);

    Flux<Void> DeleteBillsByVetId(@RequestParam(value="vetId", required = true) String vetId);
    Flux<Void> DeleteBillsByCustomerId(@RequestParam(value="customerId", required = true)String customerId);

    Mono<BillResponseDTO> updateBill(String billId, Mono<BillRequestDTO> billRequestDTO);

    Mono<Void> DeleteAllBills();

}
