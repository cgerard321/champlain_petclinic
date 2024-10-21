package com.petclinic.bffapigateway.dtos.Auth;

import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserResponseDTO {
    private long id;
    private String username;
    private String password;
    private String email;
    private boolean verified;
    private Set<Role> roles;
}
