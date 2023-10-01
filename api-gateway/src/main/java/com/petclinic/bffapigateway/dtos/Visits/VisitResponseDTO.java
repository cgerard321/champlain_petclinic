package com.petclinic.bffapigateway.dtos.Visits;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitResponseDTO {
    private String visitId;
/*    private int year;
    private int month;
    private int day;*/
@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
private LocalDateTime visitDate;
    private String description;
    private String petId;
    private String practitionerId;
/*    private int petId;
    private int practitionerId;*/
    private boolean status;
}