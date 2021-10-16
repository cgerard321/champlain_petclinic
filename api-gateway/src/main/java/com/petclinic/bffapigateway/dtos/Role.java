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
public class Role {

    private int id;
    private String name;
    private Role parent;
}
