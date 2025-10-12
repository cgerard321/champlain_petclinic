package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import com.petclinic.billing.datalayer.PaymentRequestDTO;
import com.petclinic.billing.exceptions.InvalidPaymentException;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;


@RestController
@Slf4j
@RequestMapping("/bills/customer/{customerId}/bills")
public class CustomerBillsController {

    private final BillService billService;

    public CustomerBillsController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BillResponseDTO> getBillsByCustomerId(@PathVariable("customerId") String customerId) {
        return billService.getBillsByCustomerId(customerId);
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BillResponseDTO> getBillsByStatus(@PathVariable("customerId") String customerId,
                                                  @RequestParam("status") BillStatus status) {
        return billService.getBillsByCustomerIdAndStatus(customerId, status);
    }

    @GetMapping(value = "/{billId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BillResponseDTO> getBillDetails(@PathVariable("customerId") String customerId,
                                                @PathVariable("billId") String billId) {
        return billService.getBillByCustomerIdAndBillId(customerId, billId);
    }

    @GetMapping(value = "/{billId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadBillPdf(
            @PathVariable String customerId,
            @PathVariable String billId,
            @RequestParam(name = "currency", required = false, defaultValue = "CAD") String currency) {
        return billService.generateBillPdf(customerId, billId, currency)
                .map(pdf -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=bill-" + billId + ".pdf");
                    return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
                })
                .onErrorResume(e -> {
                    log.error("Error generating PDF for billId: {} currency: {} error: {}", billId, currency, e.getMessage(), e);
                    return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @GetMapping(value = "/current-balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BigDecimal> getCurrentBalance(@PathVariable String customerId) {
        return billService.calculateCurrentBalance(customerId);
    }

    @PostMapping("/{billId}/pay")
    public Mono<ResponseEntity<BillResponseDTO>> payBill(
            @PathVariable String customerId,
            @PathVariable String billId,
            @RequestBody PaymentRequestDTO paymentRequest,
            @CookieValue("Bearer") String jwtToken) {

        String userEmail = jwtToken; //  Placeholder NOT decoded yet need jwt Utils

        return billService.processPayment(customerId, billId, paymentRequest)
                        .map(ResponseEntity::ok)
                        .onErrorResume(InvalidPaymentException.class,
                                e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()))
                        .onErrorResume(ResponseStatusException.class,
                                e -> Mono.just(ResponseEntity.status(e.getStatus()).build()));

    }
}
