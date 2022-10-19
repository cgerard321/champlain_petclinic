package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BillDTO> createBill(@Valid @RequestBody Mono<BillDTO> billDTO){
        return SERVICE.CreateBill(billDTO);
    }

    // Read Bill //
    @GetMapping(value = "/bills/{billId}")
    public Mono<BillDTO> findBill(@PathVariable("billId") String billId){
        return SERVICE.GetBill(billId);
    }

    @GetMapping(value = "/bills")
    public Flux<BillDTO> findAllBills() {
        return SERVICE.GetAllBills();
    }

    @GetMapping(value = "/bills/customer/{customerId}")
    public Flux<BillDTO> getBillsByCustomerId(@PathVariable("customerId") int customerId) {return SERVICE.GetBillsByCustomerId(customerId);}

    // Delete Bill //
    @DeleteMapping(value = "/bills/{billId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBill(@PathVariable("billId") String billId){
        return SERVICE.DeleteBill(billId);
    }

    @DeleteMapping(value = "/bills/customer/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Flux<Void> deleteBillsByCustomerId(@PathVariable("customerId") int customerId){
        return SERVICE.DeleteBillsByCustomerId(customerId);
    }
}
