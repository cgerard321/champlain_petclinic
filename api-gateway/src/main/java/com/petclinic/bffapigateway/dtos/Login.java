package com.petclinic.bffapigateway.dtos;

import lombok.Data;

@Data
public class Login {

    private String email;

    private String password;
}
