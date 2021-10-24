package com.petclinic.bffapigateway.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregateAllCombinedDetails {
    private int id;

    private String firstName;

    private String lastName;

    private String address;

    private String city;

    private String telephone;

    private  List<PetDetails> pets = new ArrayList<>();

    private  List<VetDetails> vets = new ArrayList<>();
    
    private  List<BillDetails> bills = new ArrayList<>();



}
