package com.petclinic.bffapigateway.dtos.Auth;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    private String username;
    private String password;
    private String email;
    private boolean verified;
    private Set<Role> roles;
}
