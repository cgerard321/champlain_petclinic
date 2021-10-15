package com.petclinic.bffapigateway.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder(toBuilder = true)
public class UserDetails {

    private long id;

    private String username;

    private String password;

    private String email;

    private Set<Role> roles;
}

