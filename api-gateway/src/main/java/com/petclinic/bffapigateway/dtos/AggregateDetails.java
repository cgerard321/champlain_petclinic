package com.petclinic.bffapigateway.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AggregateDetails {
    private int id;

    private String firstName;

    private String lastName;

    private String address;

    private String city;

    private String telephone;

    private UserDetails user;

    private final List<PetDetails> pets = new ArrayList<>();

    private final List<VetDetails> vets;

    private final List<BillDetails> bills = new ArrayList<>();



}
