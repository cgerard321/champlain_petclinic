package com.petclinic.bffapigateway.dtos;

import lombok.*;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitDetails {

    private String visitId;

    private int year;

    private int month;

    private int day;

    private String description;

    private int petId;

    private int practitionerId;

    private boolean status;
}
