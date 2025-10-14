package com.petclinic.bffapigateway.dtos.Bills;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    private BigDecimal amount;
    private BigDecimal taxedAmount;
    private BigDecimal interest;
    private BillStatus billStatus;
    private LocalDate dueDate;
    private Long timeRemaining;
    private boolean interestExempt;
    private Boolean archive;
}
