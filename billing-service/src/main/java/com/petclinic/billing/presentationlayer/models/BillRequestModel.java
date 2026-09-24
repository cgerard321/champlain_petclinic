package com.petclinic.billing.presentationlayer.models;

import com.petclinic.billing.dataaccesslayer.BillStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ToString
@Builder
@NoArgsConstructor
public class BillRequestModel {

    private String customerId;
    private String visitType;
    private String vetId;
    private LocalDate date;
    private BigDecimal amount;
    private BillStatus billStatus;
    private LocalDate dueDate;

    public BillRequestModel(String customerId, String visitType, String vetId, LocalDate date, BigDecimal amount, BillStatus billStatus, LocalDate dueDate)
 {
        this.customerId = customerId;
        this.visitType = visitType;
        this.vetId = vetId;
        this.date = date;
        this.amount = amount;
        this.billStatus = billStatus;
        this.dueDate = dueDate;

    }
}
