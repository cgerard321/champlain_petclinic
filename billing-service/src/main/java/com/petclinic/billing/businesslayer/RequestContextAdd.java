package com.petclinic.billing.businesslayer;

import com.petclinic.billing.dataaccesslayer.Bill;
import com.petclinic.billing.presentationlayer.DTOs.BillRequestDTO;
import com.petclinic.billing.domainclientlayer.DTOs.OwnerResponseDTO;
import com.petclinic.billing.domainclientlayer.DTOs.VetResponseDTO;
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
