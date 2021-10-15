package com.petclinic.bffapigateway.dtos;

import lombok.Data;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-15
 * Ticket: feat(APIG-CPC-354)
 */

@Data
public class Register {

    private String email;
    private String username;
    private String password;
}
