package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.PaymentRequestDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/customers/{customerId}/bills")
@Validated
public class CustomerBillController {
    
    private final BillServiceClient billService;

    @IsUserSpecific(idToMatch = {"customerId"})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BillResponseDTO> getBillsByCustomerId(@PathVariable String customerId) {
        return billService.getBillsByOwnerId(customerId);
    }

    @IsUserSpecific(idToMatch = {"customerId"})
    @GetMapping(value = "/{billId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadBillPdf(
            @PathVariable String customerId, 
            @PathVariable String billId,
            @RequestParam(name = "currency", required = false, defaultValue = "CAD") String currency) {

        return billService.downloadBillPdf(customerId, billId, currency)
                .map(pdf -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("attachment", "bill-" + billId + ".pdf");
                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(pdf);
                })
                .onErrorResume(e -> {
                    log.error("Error downloading PDF for billId: {}", billId, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
    // Get customer's current billing balance.
    @IsUserSpecific(idToMatch = { "customerId" })
    @GetMapping(value = "/current-balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Double> getCurrentBalance(@PathVariable String customerId) {
        return billService.getCurrentBalance(customerId);
    }

    @IsUserSpecific(idToMatch = {"customerId"})
    @PostMapping(value = "/{billId}/pay", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BillResponseDTO>> payBill(
            @PathVariable String customerId,
            @PathVariable String billId,
            @RequestBody PaymentRequestDTO paymentRequestDTO) {

        return billService.payBill(customerId, billId, paymentRequestDTO)
                .map(ResponseEntity::ok)
                // billing-service returns 400 for invalid payment; the client maps that to ResponseStatusException(BAD_REQUEST)
                .onErrorResume(ResponseStatusException.class, e ->
                        Mono.just(ResponseEntity.status(e.getStatusCode()).build()));
    }

    @IsUserSpecific(idToMatch = {"customerId"})
    @GetMapping(value = "/filter-by-amount", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BillResponseDTO> getBillsByAmountRange(
            @PathVariable("customerId") String customerId,
            @RequestParam("minAmount") BigDecimal minAmount,
            @RequestParam("maxAmount") BigDecimal maxAmount) {
        return billService.getBillsByAmountRange(customerId, minAmount, maxAmount);
    }

    @IsUserSpecific(idToMatch = {"customerId"})
    @GetMapping(value = "/filter-by-due-date", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BillResponseDTO> getBillsByDueDateRange(
            @PathVariable("customerId") String customerId,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {
        return billService.getBillsByDueDateRange(customerId, startDate, endDate);
    }

    @IsUserSpecific(idToMatch = {"customerId"})
    @GetMapping(value = "/filter-by-date", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BillResponseDTO> getBillsByDateRange(
            @PathVariable("customerId") String customerId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return billService.getBillsByDateRange(customerId, startDate, endDate);
    }
}
