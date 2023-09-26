package com.petclinic.billing.datalayer;

import lombok.*;

import java.time.LocalDate;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponseDTO {

    private String billId;
    private String customerId;
    private String visitType;
    private String vetId;
    private LocalDate date;
    private double amount;

    public BillResponseDTO(String customerId, String visitType, String vetId, LocalDate date, double amount) {
        this.customerId = customerId;
        this.visitType = visitType;
        this.vetId = vetId;
        this.date = date;
        this.amount = amount;
    }
}
