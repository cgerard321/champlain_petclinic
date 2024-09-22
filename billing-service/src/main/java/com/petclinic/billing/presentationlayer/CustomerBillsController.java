package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/customers")
public class CustomerBillsController {

    private final BillService billService;

    public CustomerBillsController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/{customerId}/bills")
    public ResponseEntity<Flux<BillResponseDTO>> getBillsByCustomerId(@PathVariable String customerId) {
        Flux<BillResponseDTO> bills = billService.GetBillsByCustomerId(customerId);
//        return new ResponseEntity<>(bills, HttpStatus.OK);
        return new ResponseEntity<Flux<BillResponseDTO>>(bills, HttpStatus.OK);
    }
}
