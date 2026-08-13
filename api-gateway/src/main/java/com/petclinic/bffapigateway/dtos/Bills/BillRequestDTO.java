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
public class BillRequestDTO {

    private String customerId;
    private String visitType;
    private String vetId;
    private LocalDate date;
    private BigDecimal amount;
    private BillStatus billStatus;
    private LocalDate dueDate;
}
