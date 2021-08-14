package com.petclinic.bffapigateway.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VetDetails {

        private int id;

        private String firstName;

        private String lastName;

        private final List<Specialty> specialties = new ArrayList<>();

}

