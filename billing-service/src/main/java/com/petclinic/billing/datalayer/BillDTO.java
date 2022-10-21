package com.petclinic.billing.datalayer;

import lombok.*;
import org.springframework.lang.Nullable;

import java.util.Date;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {

    private String billId;
    private int customerId;
    private String visitType;
    private String vetBillId;
    private Date date;
    private double amount;

    public BillDTO(int customerId, String visitType, String vetBillId, Date date, double amount) {
        this.customerId = customerId;
        this.visitType = visitType;
        this.vetBillId = vetBillId;
        this.date = date;
        this.amount = amount;
    }
}