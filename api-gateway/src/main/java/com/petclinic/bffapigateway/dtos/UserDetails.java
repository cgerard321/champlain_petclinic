package com.petclinic.bffapigateway.dtos;

import lombok.Data;

@Data
public class UserDetails {

    private long id;

    private String username;

    private String password;

    private String email;

    private Set<Role> roles;
}

