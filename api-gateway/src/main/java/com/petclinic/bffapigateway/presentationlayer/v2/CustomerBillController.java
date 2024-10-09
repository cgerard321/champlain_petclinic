package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/customers/{customerId}/bills")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class CustomerBillController {

    private final BillServiceClient billService;

    @IsUserSpecific(idToMatch = {"customerId"})
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByCustomerId(@PathVariable String customerId) {
        return billService.getBillsByOwnerId(customerId);
    }

    @IsUserSpecific(idToMatch = {"customerId"})
    @GetMapping(value = "/{billId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadBillPdf(
            @PathVariable String customerId, 
            @PathVariable String billId) {

        return billService.downloadBillPdf(customerId, billId)
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
}