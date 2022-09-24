package com.petclinic.billing.datalayer;

import lombok.*;
import org.springframework.lang.Nullable;

import java.util.Date;

@Data
@ToString
@NoArgsConstructor
public class BillDTO {

    private String billId;
    private int customerId;
    private String visitType;
    private Date date;
//    @Nullable
    private double amount;

    public BillDTO(int customerId, String visitType, Date date, double amount) {
        this.customerId = customerId;
        this.visitType = visitType;
        this.date = date;
        this.amount = amount;
    }

    public BillDTO(String billId, int customerId, String visitType, Date date, double amount) {
        this.billId = billId;
        this.customerId = customerId;
        this.visitType = visitType;
        this.date = date;
        this.amount = amount;
    }
}