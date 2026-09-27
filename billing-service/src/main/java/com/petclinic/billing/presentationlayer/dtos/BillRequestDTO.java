package com.petclinic.billing.presentationlayer.dtos;

import com.petclinic.billing.dataaccesslayer.BillStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillRequestDTO {

    private String customerId;
    private String visitType;
    private String vetId;
    private LocalDate date;
    private BigDecimal amount;
    private BillStatus billStatus;
    private LocalDate dueDate;

}
