package com.petclinic.bffapigateway.dtos.Bills;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class BillDetails {
    private String billId;
    private LocalDate date;
    private String customerId;
    private String vetId;
    private String visitType;
    private double amount;
    private BillStatus billStatus;
    private LocalDate dueDate;
}
