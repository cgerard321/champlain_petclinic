package com.petclinic.billing.datalayer;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bill {

    @Id
    private String id;
    private String billId;              // Should be renamed to BillUUID
    private String customerId;
    private String visitType;
    private String vetId;
    private String vetFirstName;
    private String vetLastName;
    private LocalDate date;
    private double amount;
}