package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillMapper;
import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RequestMapping("/bills")
@RestController
@Slf4j
public class BillResource {
    private final BillRepository billRepository;

    BillResource(BillRepository billRepository){
        this.billRepository = billRepository;
    }

    // Create Bill //
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public Bill createBill(@Valid @RequestBody Bill bill){
        return billRepository.save(bill);
    }

    // Read Bill //
    @GetMapping(value = "/{billId}")
    public Optional<Bill> findBill(@PathVariable("billId") int billId){
        return billRepository.findById(billId);
    }

    // Update Bill //
    @PutMapping("/{billId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateBill(@PathVariable("billId") int billId){

    }

    // Delete Bill //
    @PutMapping(value = "/{billId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBill(@PathVariable("billId") int billId){
        billRepository.findById(billId).ifPresent(entity -> billRepository.delete(entity));
    }
}
