package com.petclinic.billing.businesslayer;

import com.petclinic.billing.dataaccesslayer.Bill;
import com.petclinic.billing.domainclientlayer.dtos.CustomerResponseDTO;
import com.petclinic.billing.presentationlayer.dtos.BillRequestDTO;
import com.petclinic.billing.domainclientlayer.dtos.VetResponseDTO;
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
    private CustomerResponseDTO customerResponseDTO;

    public RequestContextAdd(BillRequestDTO billRequestDTO) {
        this.billRequestDTO = billRequestDTO;
    }
}
