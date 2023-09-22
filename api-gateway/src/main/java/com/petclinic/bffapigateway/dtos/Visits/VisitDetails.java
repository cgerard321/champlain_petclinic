package com.petclinic.bffapigateway.dtos.Visits;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private String petId = null;

    private Integer practitionerId = null;

    private String date = null;
    //private LocalDateTime visitDate = null;

    private String description = null;

    private Boolean status = null;
}
