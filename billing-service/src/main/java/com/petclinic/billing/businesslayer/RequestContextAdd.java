package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.OwnerResponseDTO;
import com.petclinic.billing.datalayer.VetResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestContextAdd {

    private BillRequestDTO billRequestDTO;
    private Bill bill;
    private VetResponseDTO vetDTO;
    private OwnerResponseDTO ownerResponseDTO;

    public RequestContextAdd(BillRequestDTO billRequestDTO) {
        this.billRequestDTO = billRequestDTO;
    }
}
