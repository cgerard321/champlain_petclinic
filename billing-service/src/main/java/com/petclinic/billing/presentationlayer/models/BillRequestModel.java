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
public class BillRequestModel {

    private String customerId;
    private String visitType;
    private String vetId;
    private LocalDate date;
    private BigDecimal amount;
    private BillStatus billStatus;
    private LocalDate dueDate;

}
