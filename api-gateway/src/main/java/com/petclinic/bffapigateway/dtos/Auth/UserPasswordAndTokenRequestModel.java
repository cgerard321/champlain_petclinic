package com.petclinic.bffapigateway.dtos.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserPasswordAndTokenRequestModel {
    private String password;
    private String token;
}
