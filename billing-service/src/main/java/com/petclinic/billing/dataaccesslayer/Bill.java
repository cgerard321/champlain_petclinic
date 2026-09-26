package com.petclinic.billing.dataaccesslayer;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bill {

    @Id
    private String id;
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
    @Builder.Default
    private boolean isInterestExempt = false;
    @Field("archived")
    private Boolean isArchived = false;
}