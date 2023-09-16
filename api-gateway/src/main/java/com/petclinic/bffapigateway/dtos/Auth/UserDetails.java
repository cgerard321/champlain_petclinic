package com.petclinic.bffapigateway.dtos.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserDetails {

    private long id;

    private String username;

    private String password;

    private String email;

    private Set<Role> roles;
}

