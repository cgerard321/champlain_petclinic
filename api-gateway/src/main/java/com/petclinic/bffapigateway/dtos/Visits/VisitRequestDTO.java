package com.petclinic.bffapigateway.dtos.Visits;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitRequestDTO {

    private LocalDateTime visitDate;
/*   private int year;
    private int month;
    private int day;*/
    private String description;
    private String petId;
    private String practitionerId;
/*    private int petId;
    private int practitionerId;*/
    private boolean status;

    public VisitRequestDTO(LocalDateTime now, String description, String petId, String practitionerId) {
        this.visitDate = now;
        this.description = description;
        this.petId =  petId;
        this.practitionerId = practitionerId;
        this.status = true;
    }
}
