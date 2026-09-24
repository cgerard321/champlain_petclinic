package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.dataaccesslayer.*;
import com.petclinic.billing.presentationlayer.models.BillRequestModel;
import com.petclinic.billing.presentationlayer.models.BillResponseModel;
import lombok.extern.slf4j.Slf4j;
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

    //Simple CRUD
    @GetMapping(value = "/bills", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseModel> getAllBills() {
        return billService.getAllBills();
    }

    @GetMapping("/bills")
    public Flux<BillResponseModel> getAllBillsByPage(@RequestParam Optional<Integer> page,
                                                     @RequestParam Optional<Integer> size,
                                                     @RequestParam(required = false) String billId,
                                                     @RequestParam(required = false) String customerId,
                                                     @RequestParam(required = false) String customerFirstName,
                                                     @RequestParam(required = false) String customerLastName,
                                                     @RequestParam(required = false) String visitType,
                                                     @RequestParam(required = false) String vetId,
                                                     @RequestParam(required = false) String vetFirstName,
                                                     @RequestParam(required = false) String vetLastName) {

        if (page.orElse(0) < 0 || size.orElse(10) <= 0) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page or size"));
        }

        return billService.getAllBillsByPage(PageRequest.of(page.get(), size.get()), billId, customerId,
                customerFirstName, customerLastName, visitType, vetId, vetFirstName, vetLastName);
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

    // Read Bill //
    @GetMapping(value = "/bills/{billId}")
    public Mono<BillResponseModel> getBillByBillId(@PathVariable String billId) {
        return billService.getBillByBillId(billId);
    }

    @PostMapping("/bills")
    public Mono<ResponseEntity<BillResponseModel>> createBill(@RequestBody Mono<BillRequestModel> billDTO,
                                                              @RequestParam(defaultValue = "false") boolean sendEmail,
                                                              @RequestParam(defaultValue = "CAD", required = false) String currency,
                                                              @CookieValue("Bearer") String jwtToken
    ) {
        return billDTO
                .flatMap(dto -> {
                    if (dto.getCustomerId() == null || dto.getCustomerId().trim().isEmpty()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer ID is required"));
                    }
                    if (dto.getVetId() == null || dto.getVetId().trim().isEmpty()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vet ID is required"));
                    }
                    if (dto.getAmount() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill amount is required"));
                    }
                    if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill amount must be greater than zero"));
                    }
                    if (dto.getBillStatus() == null) {
                        dto.setBillStatus(BillStatus.UNPAID);
                        log.debug("Auto-set bill status to UNPAID");
                    }
                    if (dto.getDate() == null) {
                        dto.setDate(LocalDate.now());
                        log.debug("Auto-set bill date to today: {}", dto.getDate());
                    }
                    if (dto.getDate().isBefore(LocalDate.now())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Bill date cannot be in the past. Please use today's date or a future date."));
                    }
                    if (dto.getDueDate() == null) {
                        dto.setDueDate(dto.getDate().plusDays(45));
                        log.debug("Auto-suggested due date (45 days): {}", dto.getDueDate());
                    }
                    if (dto.getBillStatus() == BillStatus.OVERDUE) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Cannot create bill with OVERDUE status. Use PAID or UNPAID instead. OVERDUE status is automatically set based on due date."));
                    }
                    return billService.createBill(Mono.just(dto), sendEmail, currency, jwtToken);
                })
                .map(bill -> ResponseEntity.status(HttpStatus.CREATED).body(bill));
    }

    @PutMapping(value = "/bills/{billId}")
    public Mono<ResponseEntity<BillResponseModel>> updateBill(@PathVariable String billId, @RequestBody Mono<BillRequestModel> billRequestDTO) {
        return billService.updateBill(billId, billRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/bills/{billId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBill(@PathVariable("billId") String billId) {
        return billService.deleteBill(billId);
    }

    // Bills/Filters
    @GetMapping("/bills/bills-count")
    public Mono<ResponseEntity<Long>> getTotalNumberOfBills() {
        return billService.getAllBills().count()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @GetMapping("/bills/bills-filtered-count")
    public Mono<Long> getNumberOfBillsWithFilters(@RequestParam(required = false) String billId,
                                                  @RequestParam(required = false) String customerId,
                                                  @RequestParam(required = false) String customerFirstName,
                                                  @RequestParam(required = false) String customerLastName,
                                                  @RequestParam(required = false) String visitType,
                                                  @RequestParam(required = false) String vetId,
                                                  @RequestParam(required = false) String vetFirstName,
                                                  @RequestParam(required = false) String vetLastName
    ) {
        return billService.getNumberOfBillsWithFilters(billId, customerId, customerFirstName, customerLastName, visitType, vetId,
                vetFirstName, vetLastName);
    }

    @GetMapping(value = "/bills/visitType/{visitType}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseModel> getAllBillsByVisitType(@PathVariable String visitType) {
        return billService.getAllBillsByVisitType(visitType);
    }

    @GetMapping(value = "/bills/paid", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseModel> getAllPaidBills() {
        return billService.getAllBillsByStatus(BillStatus.PAID);
    }

    @GetMapping(value = "/bills/unpaid", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseModel> getAllUnpaidBills() {
        return billService.getAllBillsByStatus(BillStatus.UNPAID);
    }

    @GetMapping(value = "/bills/overdue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseModel> getAllOverdueBills() {
        return billService.getAllBillsByStatus(BillStatus.OVERDUE);
    }

    @GetMapping(value = "/bills/month", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseModel> getBillsByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        if (year < 0 || month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid year or month");
        }

        return billService.getBillsByMonth(year, month);
    }

    // Bills/BillId/Filter
    @GetMapping("/bills/{billId}/total")
    public Mono<BigDecimal> getTotal(@PathVariable String billId) {
        return billService.getBillByBillId(billId)
                .map(bill -> bill.getAmount().add(bill.getInterest()));
    }

    @GetMapping("/bills/{billId}/interest")
    public Mono<BigDecimal> getInterest(@PathVariable String billId) {
        return billService.getBillByBillId(billId)
                .map(BillResponseModel::getInterest);
    }

    // Bills/Vet
    @GetMapping(value = "/bills/vet/{vetId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseModel> getBillsByVetId(@PathVariable("vetId") String vetId) {
        return billService.getBillsByVetId(vetId);
    }

    @GetMapping(value = "/bills/vet/{vetFirstName}/{vetLastName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseModel> getAllBillsByVetName(@PathVariable String vetFirstName, @PathVariable String vetLastName) {
        return billService.getAllBillsByVetName(vetFirstName, vetLastName);
    }

    // Delete
    @DeleteMapping(value = "/bills/vet/{vetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Flux<Void> deleteBillsByVetId(@PathVariable("vetId") String vetId) {
        return billService.deleteBillsByVetId(vetId);
    }

    // Bills/Customer
    @GetMapping(value = "/bills/customer/{customerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseModel> getBillsByCustomerId(@PathVariable("customerId") String customerId) {
        return billService.getBillsByCustomerId(customerId);
    }

    @GetMapping(value = "/bills/owner/{customerFirstName}/{customerLastName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseModel> getAllBillsByCustomerName(@PathVariable String customerFirstName, @PathVariable String customerLastName) {
        return billService.getAllBillsByCustomerName(customerFirstName, customerLastName);
    }

    @DeleteMapping(value = "/bills/customer/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Flux<Void> deleteBillsByCustomerId(@PathVariable("customerId") String customerId) {
        return billService.deleteBillsByCustomerId(customerId);
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

    //Patches
    @PatchMapping("/bills/{billId}/exempt-interest")
    public Mono<ResponseEntity<Void>> exemptInterest(@PathVariable String billId, @RequestParam boolean exempt) {
        return billService.setInterestExempt(billId, exempt)
                .thenReturn(ResponseEntity.ok().build());
    }

    @PatchMapping("/bills/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Object>> archiveBill() {
        return billService.archiveBill()
                .then(Mono.just(ResponseEntity.noContent().build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    // Delete All Bills
    @DeleteMapping(value = "/bills")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAllBills() {
        return billService.deleteAllBills();
    }
}