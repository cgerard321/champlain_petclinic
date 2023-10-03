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
    private String visitType;
    private String vetId;
    private LocalDate date;
    private double amount;

    private BillStatus billStatus;


    }
