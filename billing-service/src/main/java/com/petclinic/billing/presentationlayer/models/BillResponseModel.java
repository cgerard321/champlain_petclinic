package com.petclinic.billing.presentationlayer.models;

import com.petclinic.billing.dataaccesslayer.BillStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponseModel {

    private String billId;
    private String customerId;
    private String customerFirstName;
    private String customerLastName;
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
    private Boolean archive;   
    private boolean isInterestExempt;
}
