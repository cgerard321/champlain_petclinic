package com.petclinic.bffapigateway.dtos.Bills;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillResponseDTO {

    private String billId;
    private int customerId;
    private String visitType;
    private String vetId;
    private Date date;
    private double amount;
}
