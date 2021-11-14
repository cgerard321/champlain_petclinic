package com.petclinic.bffapigateway.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class VisitDetails {

    private String visitId = null;

    private Integer petId = null;

    private Integer practitionerId = null;

    private String date = null;

    private String description = null;

    private Boolean status = null;
}
