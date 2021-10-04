package com.petclinic.bffapigateway.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Data
@NoArgsConstructor
public class VisitDetails {

    private Integer id = null;

    private Integer petId = null;

    private String date = null;

    private String description = null;

    private Integer practitionerId = null;

    private Boolean status = null;
}
