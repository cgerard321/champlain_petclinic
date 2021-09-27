package com.petclinic.bffapigateway.dtos;

import lombok.Data;

@Data
public class UserDetails {

    private int id;

    private String username;

    private String password;

    private String email;
}
