package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/customers")
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class CustomerBillsController {

    private final BillServiceClient billServiceClient;

    @GetMapping(value = "/{customerId}/bills", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByCustomerId(@PathVariable String customerId) {
        return billServiceClient.getBillsByCustomerId(customerId);
    }
}
