package com.petclinic.bffapigateway.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VetDetails {

        private int id;

        private int vetId;

        private String firstName;

        private String lastName;

        private String email;

        private String phoneNumber;

        private String resume;

        private String workday;

        private Integer enabled;

        private final List<Specialty> specialties = new ArrayList<>();

}

