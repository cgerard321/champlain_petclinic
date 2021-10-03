package com.petclinic.bffapigateway.dtos;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Value
public class Visits {

    private List<VisitDetails> items = new ArrayList<>();

}

