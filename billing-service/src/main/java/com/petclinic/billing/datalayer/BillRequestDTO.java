package com.petclinic.billing.datalayer;

import lombok.*;

import java.util.Date;

@Data
@ToString
@Builder
@NoArgsConstructor
public class BillRequestDTO {

    private int customerId;
    private String visitType;
    private String vetId;
    private Date date;
    private double amount;

    public BillRequestDTO(int customerId, String visitType, String vetId, Date date, double amount) {
        this.customerId = customerId;
        this.visitType = visitType;
        this.vetId = vetId;
        this.date = date;
        this.amount = amount;
    }
}
