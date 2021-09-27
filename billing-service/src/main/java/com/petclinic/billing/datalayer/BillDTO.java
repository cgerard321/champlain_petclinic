package com.petclinic.billing.datalayer;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class BillDTO {

    private Date date;
    private String visitType;
    private double amount;
}
