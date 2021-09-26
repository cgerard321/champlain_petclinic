package com.petclinic.bffapigateway.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

//created this class in dtos folder
@Data
public class BillDetails {
    private int billId;
    private Date date;
    private String visitType;
    private double amount;
//    private final List<BillDetails> bills = new ArrayList<>();
//
//
//    public List<BillDetails> getBill() {
//        return bills.stream()
//                .map(BillDetails::getBill)
//                .collect(toList());
//    }
}
