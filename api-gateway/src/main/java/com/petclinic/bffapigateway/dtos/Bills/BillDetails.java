package com.petclinic.bffapigateway.dtos.Bills;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Builder
@Data
public class BillDetails {
    private String billId;
    private LocalDate date;
    private int customerId;
    private String vetId;
    private String visitType;
    private double amount;

}
