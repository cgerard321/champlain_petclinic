package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestContextAdd {

    private BillRequestDTO billRequestDTO;
    private Bill bill;
    private VetResponseDTO vetResponseDTO;
    private OwnerResponseDTO ownerResponseDTO;

    public RequestContextAdd(BillRequestDTO billRequestDTO) {
        this.billRequestDTO = billRequestDTO;
    }
}
