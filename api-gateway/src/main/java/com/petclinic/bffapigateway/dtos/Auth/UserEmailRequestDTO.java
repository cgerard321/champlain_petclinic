package com.petclinic.bffapigateway.dtos.Auth;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserEmailRequestDTO {
    private String email;
    private String url;
}
