package com.petclinic.billing.datalayer;

import lombok.*;

import java.util.Date;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {

    private String billId;
    private String ownerId;
    private String visitType;
    private String vetId;
    private Date date;
    private double amount;

//    public BillDTO(String ownerId, String visitType, String vetId, Date date, double amount) {
//        this.ownerId = ownerId;
//        this.visitType = visitType;
//        this.vetId = vetId;
//        this.date = date;
//        this.amount = amount;
//    }
}