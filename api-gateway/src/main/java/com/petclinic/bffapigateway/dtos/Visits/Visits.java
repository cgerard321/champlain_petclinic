package com.petclinic.bffapigateway.dtos.Visits;

import com.petclinic.bffapigateway.dtos.Vets.VisitDetails;
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

