package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.VetDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestContextAdd {

    private BillRequestDTO billRequestDTO;
    private Bill bill;
    private VetDTO vetDTO;

    public RequestContextAdd(BillRequestDTO billRequestDTO) {
        this.billRequestDTO = billRequestDTO;
    }
}
