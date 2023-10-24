package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Optional;

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

    //to be changed
//    @GetMapping("/bills-pagination")
//    public Flux<BillResponseDTO> getAllBillsByPage(@RequestParam Optional<Integer> page,
//                                                   @RequestParam Optional<Integer> size,
//                                                   ) {
//        return SERVICE.getAllBillsByPage(
//                PageRequest.of(page.orElse(0),size.orElse(5)));
//    }
//
    //to be changed
    @GetMapping("/bills/bills-count")
    public Mono<ResponseEntity<Long>> getTotalNumberOfBills(){
        return SERVICE.GetAllBills().count()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @GetMapping("/bills/bills-pagination")
    public Flux<BillResponseDTO> getAllBillsByPage(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam(required = false) String billId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String ownerFirstName,
            @RequestParam(required = false) String ownerLastName,
            @RequestParam(required = false) String visitType,
            @RequestParam(required = false) String vetId,
            @RequestParam(required = false) String vetFirstName,
            @RequestParam(required = false) String vetLastName
    ){
        return SERVICE.getAllBillsByPage(
                PageRequest.of(page.orElse(0),size.orElse(5)), billId, customerId, ownerFirstName, ownerLastName,
                visitType, vetId, vetFirstName, vetLastName);
    }

    @GetMapping("/bills/bills-filtered-count")
    public Mono<Long> getNumberOfBillsWithFilters(@RequestParam(required = false) String billId,
                                                        @RequestParam(required = false) String customerId,
                                                        @RequestParam(required = false) String ownerFirstName,
                                                        @RequestParam(required = false) String ownerLastName,
                                                        @RequestParam(required = false) String visitType,
                                                        @RequestParam(required = false) String vetId,
                                                        @RequestParam(required = false) String vetFirstName,
                                                        @RequestParam(required = false) String vetLastName
    ){

        return SERVICE.getNumberOfBillsWithFilters(billId, customerId, ownerFirstName, ownerLastName, visitType, vetId,
                vetFirstName, vetLastName);
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

    @DeleteMapping(value = "/bills")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAllBills(){
        return SERVICE.DeleteAllBills();
    }

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
