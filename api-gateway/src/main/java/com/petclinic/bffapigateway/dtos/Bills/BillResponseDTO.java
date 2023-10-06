package com.petclinic.bffapigateway.dtos.Bills;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillResponseDTO {

    private String billId;
    private String customerId;
    private String visitType;
    private String vetId;
    private LocalDate date;
    private double amount;
    private BillStatus billStatus;
}
