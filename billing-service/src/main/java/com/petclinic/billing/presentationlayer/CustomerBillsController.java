package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        return billService.GetBillsByCustomerId(customerId);
    }

    @GetMapping(value = "/status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByStatus(@PathVariable("customerId") String customerId,
                                                  @RequestParam("status") BillStatus status) {
        return billService.GetBillsByCustomerIdAndStatus(customerId, status);
    }

    @GetMapping(value = "/{billId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BillResponseDTO> getBillDetails(@PathVariable("customerId") String customerId,
                                                @PathVariable("billId") String billId) {
        return billService.GetBillByCustomerIdAndBillId(customerId, billId);
    }

    @GetMapping(value = "/{billId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadBillPdf(@PathVariable String customerId,
                                                        @PathVariable String billId) {
        return billService.generateBillPdf(customerId, billId)
                .map(pdf -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("attachment", "bill-" + billId + ".pdf");
                    return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
                })
                .onErrorResume(e -> {
                    log.error("Error generating PDF for billId: {}", billId, e);
                    return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }
}