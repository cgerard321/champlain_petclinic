package com.petclinic.bffapigateway.dtos;

import lombok.Data;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Data
public class PetType {
    private Integer id;

    private String name;
}

