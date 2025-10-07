package com.petclinic.billing.datalayer;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Data
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bill {

    @Id
    private String id;
    private String billId;              // Should be renamed to BillUUID
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
    @Field("archive")
    private Boolean archive = false;
}