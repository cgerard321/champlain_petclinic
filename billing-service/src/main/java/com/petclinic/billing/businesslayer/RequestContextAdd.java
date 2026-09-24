package com.petclinic.billing.businesslayer;

import com.petclinic.billing.dataaccesslayer.Bill;
import com.petclinic.billing.domainclientlayer.models.CustomerResponseModel;
import com.petclinic.billing.presentationlayer.models.BillRequestModel;
import com.petclinic.billing.domainclientlayer.models.VetResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestContextAdd {

    private BillRequestModel billRequestModel;
    private Bill bill;
    private VetResponseModel vetResponseModel;
    private CustomerResponseModel customerResponseModel;

    public RequestContextAdd(BillRequestModel billRequestModel) {
        this.billRequestModel = billRequestModel;
    }
}
