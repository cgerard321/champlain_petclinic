package com.petclinic.billing.datalayer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bill {

    @Id
    private String id;

    private String billId;              // Should be renamed to BillUUID
    private int customerId;
    private String visitType;
    private String vetBillId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date visitDate = new Date();
    private double amount;
}