package com.petclinic.billing.datalayer;

import lombok.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ToString
@Builder
@NoArgsConstructor
public class BillRequestDTO {

    public interface UpdateValidation {
    }

    private String customerId;
    private String visitType;
    private String vetId;
    private LocalDate date;
    private BigDecimal amount;
    private BillStatus billStatus;
    @NotNull(groups = UpdateValidation.class, message = "Due date is required")
    private LocalDate dueDate;

    public BillRequestDTO(String customerId, String visitType, String vetId, LocalDate date, BigDecimal amount, BillStatus billStatus, LocalDate dueDate)
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
