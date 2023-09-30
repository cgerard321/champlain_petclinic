package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import javax.validation.Valid;

@RestController
@Slf4j
public class BillResource {
    private final BillService SERVICE;

    BillResource(BillService service){
        this.SERVICE = service;
    }

    // Create Bill //
    @PostMapping("/bills")
    public Mono<ResponseEntity<BillResponseDTO>> createBill(@Valid @RequestBody Mono<BillRequestDTO> billDTO){
        return SERVICE.CreateBill(billDTO)
                .map(e -> ResponseEntity.status(HttpStatus.CREATED).body(e))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    // Read Bill //
    @GetMapping(value = "/bills/{billId}")
    public Mono<BillResponseDTO> getBillByBillId(@PathVariable String billId){
        return SERVICE.getBillByBillId(billId);
    }

    @GetMapping(value = "/bills", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> findAllBills() {
        return SERVICE.GetAllBills();
    }



    @GetMapping(value = "/bills/paid", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> findAllPaidBills() {
        return SERVICE.GetAllBillsByStatus(BillStatus.PAID);
    }

    @GetMapping(value = "/bills/unpaid", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> findAllUnpaidBills() {
        return SERVICE.GetAllBillsByStatus(BillStatus.UNPAID);
    }

    @GetMapping(value = "/bills/overdue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> findAllOverdueBills() {
        return SERVICE.GetAllBillsByStatus(BillStatus.OVERDUE);
    }

    @PutMapping(value ="/bills/{billId}")
    public Mono<ResponseEntity<BillResponseDTO>> updateBill(@PathVariable String billId, @RequestBody Mono<BillRequestDTO> billRequestDTO){
        return SERVICE.updateBill(billId, billRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/bills/customer/{customerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByCustomerId(@PathVariable("customerId") String customerId)
    {
        return SERVICE.GetBillsByCustomerId(customerId);
    }


    @GetMapping(value = "/bills/vet/{vetId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByVetId(@PathVariable("vetId") String vetId)
    {
        return SERVICE.GetBillsByVetId(vetId);
    }

    // Delete Bill //
    @DeleteMapping(value = "/bills/{billId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBill(@PathVariable("billId") String billId){
        return SERVICE.DeleteBill(billId);
    }

    @DeleteMapping (value = "/bills/vet/{vetId}")
    @ResponseStatus (HttpStatus.NO_CONTENT)
    public Flux<Void> deleteBillsByVetId (@PathVariable("vetId") String vetId){
        return SERVICE.DeleteBillsByVetId(vetId);
    }

    @DeleteMapping (value = "/bills/customer/{customerId}")
    @ResponseStatus (HttpStatus.NO_CONTENT)
    public Flux<Void> deleteBillsByCustomerId (@PathVariable("customerId") String customerId){
        return SERVICE.DeleteBillsByCustomerId(customerId);
    }


}
