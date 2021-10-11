package com.petclinic.bffapigateway.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;


@Data
public class BillDetails {
    private int billId;
    private Date date;
    private int customerId;
    private String visitType;
    private double amount;

}
