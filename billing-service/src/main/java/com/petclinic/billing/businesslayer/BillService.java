package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface BillService {
    BillDTO GetBill(@RequestParam(value = "billId", required = true) int billId);

    BillDTO CreateBill(@RequestBody BillDTO model);

    void DeleteBill(@RequestParam(value = "billId", required = true) int billId);
}
