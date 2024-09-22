package com.petclinic.billing.datalayer;

import lombok.*;

import java.time.LocalDate;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponseDTO {

    private String billId;
    private String customerId;
    private String ownerFirstName;
    private String ownerLastName;
    private String visitType;
    private String vetId;
    private String vetFirstName;
    private String vetLastName;
    private LocalDate date;
    private double amount;
    private double taxedAmount;
    private BillStatus billStatus;
    private LocalDate dueDate;


    }
