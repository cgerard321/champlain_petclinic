package com.petclinic.billing.datalayer;

import lombok.*;

import java.time.LocalDate;

@Data
@ToString
@Builder
@NoArgsConstructor
public class BillRequestDTO {

    private String customerId;
    private String visitType;
    private String vetId;
    private LocalDate date;
    private double amount;
    private BillStatus billStatus;


    public BillRequestDTO(String customerId, String visitType, String vetId, LocalDate date, double amount, BillStatus billStatus)
 {
        this.customerId = customerId;
        this.visitType = visitType;
        this.vetId = vetId;
        this.date = date;
        this.amount = amount;
        this.billStatus = billStatus;

    }
}
