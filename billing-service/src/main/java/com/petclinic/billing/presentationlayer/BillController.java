package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@Slf4j
public class BillController {
    private final BillService billService;
    
    BillController(BillService billService) {
        this.billService = billService;
    }

    // Create Bill //
    @PostMapping("/bills")
    public Mono<ResponseEntity<BillResponseDTO>> createBill(@RequestBody Mono<BillRequestDTO> billDTO) {
        return billDTO
                .flatMap(dto -> {
                    // Required field validation
                    if (dto.getCustomerId() == null || dto.getCustomerId().trim().isEmpty()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer ID is required"));
                    }
                    
                    if (dto.getVetId() == null || dto.getVetId().trim().isEmpty()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vet ID is required"));
                    }
                    
                    // Amount validation
                    if (dto.getAmount() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill amount is required"));
                    }
                    
                    if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill amount must be greater than zero"));
                    }
                    
                    // Auto-set bill status to UNPAID if not provided
                    if (dto.getBillStatus() == null) {
                        dto.setBillStatus(BillStatus.UNPAID);
                        log.debug("Auto-set bill status to UNPAID");
                    }
                    
                    // Auto-set created date to today if not provided
                    if (dto.getDate() == null) {
                        dto.setDate(LocalDate.now());
                        log.debug("Auto-set bill date to today: {}", dto.getDate());
                    }
                    
                    // Prevent bills from being created with past dates
                    if (dto.getDate().isBefore(LocalDate.now())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Bill date cannot be in the past. Please use today's date or a future date."));
                    }
                    
                    // Gentle due date suggestion for pet clinic (45 days - flexible for pet owners)
                    if (dto.getDueDate() == null) {
                        dto.setDueDate(dto.getDate().plusDays(45));
                        log.debug("Auto-suggested due date (45 days): {}", dto.getDueDate());
                    }
                    
                    // Prevent manual creation of OVERDUE bills - status is automatically managed by system
                    if (dto.getBillStatus() == BillStatus.OVERDUE) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Cannot create bill with OVERDUE status. Use PAID or UNPAID instead. OVERDUE status is automatically set based on due date."));
                    }
                    return billService.createBill(Mono.just(dto));
                })
                .map(bill -> ResponseEntity.status(HttpStatus.CREATED).body(bill));
    }


    // Read Bill //
    @GetMapping(value = "/bills/{billId}")
    public Mono<BillResponseDTO> getBillByBillId(@PathVariable String billId) {
        return billService.getBillByBillId(billId);
    }

    @GetMapping(value = "/bills", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBills() {
        return billService.getAllBills();
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
    public Mono<ResponseEntity<Long>> getTotalNumberOfBills() {
        return billService.getAllBills().count()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

//    @GetMapping("/bills/bills-pagination")
//    public Flux<BillResponseDTO> getAllBillsByPage(
//            @RequestParam Optional<Integer> page,
//            @RequestParam Optional<Integer> size,
//            @RequestParam(required = false) String billId,
//            @RequestParam(required = false) String customerId,
//            @RequestParam(required = false) String ownerFirstName,
//            @RequestParam(required = false) String ownerLastName,
//            @RequestParam(required = false) String visitType,
//            @RequestParam(required = false) String vetId,
//            @RequestParam(required = false) String vetFirstName,
//            @RequestParam(required = false) String vetLastName
//    ){
//        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(10));
//        return SERVICE.getAllBillsByPage(pageable, billId, customerId, ownerFirstName, ownerLastName, visitType, vetId, vetFirstName, vetLastName);
//    }

    @GetMapping("/bills")
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
            @RequestParam(required = false) String vetLastName) {

        if (page.orElse(0) < 0 || size.orElse(10) <= 0) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page or size"));
        }

        return billService.getAllBillsByPage(PageRequest.of(page.get(), size.get()), billId, customerId,
                ownerFirstName, ownerLastName, visitType, vetId, vetFirstName, vetLastName);
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
    ) {

        return billService.getNumberOfBillsWithFilters(billId, customerId, ownerFirstName, ownerLastName, visitType, vetId,
                vetFirstName, vetLastName);
    }

    @GetMapping(value = "/bills/owner/{ownerFirstName}/{ownerLastName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBillsByOwnerName(@PathVariable String ownerFirstName, @PathVariable String ownerLastName) {
        return billService.getAllBillsByOwnerName(ownerFirstName, ownerLastName);
    }

    @GetMapping(value = "/bills/vet/{vetFirstName}/{vetLastName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBillsByVetName(@PathVariable String vetFirstName, @PathVariable String vetLastName) {
        return billService.getAllBillsByVetName(vetFirstName, vetLastName);
    }

    @GetMapping(value = "/bills/visitType/{visitType}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBillsByVisitType(@PathVariable String visitType) {
        return billService.getAllBillsByVisitType(visitType);
    }

    @GetMapping(value = "/bills/paid", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllPaidBills() {
        return billService.getAllBillsByStatus(BillStatus.PAID);
    }

    @GetMapping(value = "/bills/unpaid", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllUnpaidBills() {
        return billService.getAllBillsByStatus(BillStatus.UNPAID);
    }

    @GetMapping(value = "/bills/overdue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllOverdueBills() {
        return billService.getAllBillsByStatus(BillStatus.OVERDUE);
    }

    @PutMapping(value = "/bills/{billId}")
    public Mono<ResponseEntity<BillResponseDTO>> updateBill(@PathVariable String billId, @RequestBody Mono<BillRequestDTO> billRequestDTO) {
        return billService.updateBill(billId, billRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/bills/customer/{customerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByCustomerId(@PathVariable("customerId") String customerId) {
        return billService.getBillsByCustomerId(customerId);
    }


    @GetMapping(value = "/bills/vet/{vetId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByVetId(@PathVariable("vetId") String vetId) {
        return billService.getBillsByVetId(vetId);
    }

    // Delete Bill //

    @DeleteMapping(value = "/bills")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAllBills() {
        return billService.deleteAllBills();
    }

    @DeleteMapping(value = "/bills/{billId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBill(@PathVariable("billId") String billId) {
        return billService.deleteBill(billId);
    }

    @DeleteMapping(value = "/bills/vet/{vetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Flux<Void> deleteBillsByVetId(@PathVariable("vetId") String vetId) {
        return billService.deleteBillsByVetId(vetId);
    }

    @DeleteMapping(value = "/bills/customer/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Flux<Void> deleteBillsByCustomerId(@PathVariable("customerId") String customerId) {
        return billService.deleteBillsByCustomerId(customerId);
    }

    @GetMapping(value = "/bills/month", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        if (year < 0 || month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid year or month");
        }

        return billService.getBillsByMonth(year, month);
    }

    @PatchMapping("/bills/{billId}/exempt-interest")
    public Mono<ResponseEntity<Void>> exemptInterest(@PathVariable String billId, @RequestParam boolean exempt) {
        return billService.setInterestExempt(billId, exempt)
            .thenReturn(ResponseEntity.ok().build());
    }

    @GetMapping("/bills/{billId}/interest")
    public Mono<BigDecimal> getInterest(@PathVariable String billId) {
        return billService.getBillByBillId(billId)
                .map(BillResponseDTO::getInterest);
    }
    @GetMapping("/bills/{billId}/total")
    public Mono<BigDecimal> getTotal(@PathVariable String billId) {
        return billService.getBillByBillId(billId)
                .map(bill -> bill.getAmount().add(bill.getInterest()));
    }
    @PatchMapping("/bills/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Object>> archiveBill() {
        return billService.archiveBill()
                .then(Mono.just(ResponseEntity.noContent().build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/bills/{billId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadStaffBillPdf(
            @PathVariable String billId,
            @RequestParam(name = "currency", required = false, defaultValue = "CAD") String currency) {

        return billService.generateStaffBillPdf(billId, currency)
                .map(pdf -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=staff-bill-" + billId + ".pdf");
                    log.info("Staff PDF generated for bill {}", billId);
                    return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
                })
                .onErrorResume(e -> {
                    log.error("Error generating staff PDF for billId: {}, currency: {}, error: {}", billId, currency, e.getMessage(), e);
                    return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }
}
