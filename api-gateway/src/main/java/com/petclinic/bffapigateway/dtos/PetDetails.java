package com.petclinic.bffapigateway.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Data
public class PetDetails {

    private int id;

    private String name;

    private String birthDate;

    private PetType type;

    private final List<VisitDetails> visits = new ArrayList<>();

}

