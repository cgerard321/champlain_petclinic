package com.petclinic.bffapigateway.dtos.Visits;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime visitDate = null;

    private String description = null;

    private Status status = null;
}
