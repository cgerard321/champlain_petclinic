package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The following class is customer-specific controller
 * that exposes endpoints only for customer-specific operations
 * such as, viewing bills and filtering by status.
 */
@RestController
@Slf4j
@RequestMapping("/customers/{customerId}/bills")
public class CustomerBillsController {

    private final BillService billService;

    public CustomerBillsController(BillService billService) {
        this.billService = billService;
    }

    // Endpoint to get all bills for a specific customer
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BillResponseDTO> getBillsByCustomerId(@PathVariable("customerId") String customerId) {
        return billService.GetBillsByCustomerId(customerId);
    }

    // Endpoint to filter bills by status for a customer (Paid, Unpaid, Overdue)
    @GetMapping(value = "/status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByStatus(
            @PathVariable("customerId") String customerId,
            @RequestParam("status") BillStatus status
    ) {
        return billService.GetBillsByCustomerIdAndStatus(customerId, status);
    }

    // Endpoint to view details of a specific bill
    @GetMapping(value = "/{billId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BillResponseDTO> getBillDetails(
            @PathVariable("customerId") String customerId,
            @PathVariable("billId") String billId
    ) {
        return billService.GetBillByCustomerIdAndBillId(customerId, billId);
    }
}
