package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.BillResponseDTO;
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
    public Mono<BillResponseDTO> createBill(@Valid @RequestBody Mono<BillRequestDTO> billDTO){
        return SERVICE.CreateBill(billDTO);
    }

    // Read Bill //
    @GetMapping(value = "/bills/{billId}")
    public Mono<BillResponseDTO> findBill(@PathVariable("billId") String billId){
        return SERVICE.GetBill(billId);
    }

    @GetMapping(value = "/bills")
    public Flux<BillResponseDTO> findAllBills() {
        return SERVICE.GetAllBills();
    }

    @PutMapping(value ="/bills/{billId}")
    public Mono<BillDTO> updateBill(@PathVariable String billId, @RequestBody Mono<BillDTO> billDTOMono){
        return SERVICE.updateBill(billId, billDTOMono);
    }

    @GetMapping(value = "/bills/customer/{customerId}")
    public Flux<BillResponseDTO> getBillsByCustomerId(@PathVariable("customerId") int customerId)
    {
        return SERVICE.GetBillsByCustomerId(customerId);
    }


    @GetMapping(value = "/bills/vet/{vetId}")
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
    public Flux<Void> deleteBillsByCustomerId (@PathVariable("customerId") int customerId){
        return SERVICE.DeleteBillsByCustomerId(customerId);
    }


}
