package com.petclinic.bffapigateway.dtos.Bills;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillRequestDTO {

    private int customerId;
    private String visitType;
    private String vetId;
    private LocalDate date;
    private double amount;
}
