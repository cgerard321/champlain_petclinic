package com.petclinic.bffapigateway.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class BillDetails {
    private String billId;
    private Date date;
    private String ownerId;
    private String vetId;
    private String visitType;
    private double amount;

}
