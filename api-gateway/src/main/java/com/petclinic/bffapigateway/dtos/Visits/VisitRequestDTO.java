package com.petclinic.bffapigateway.dtos.Visits;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitRequestDTO {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime visitDate;
/*   private int year;
    private int month;
    private int day;*/
    private String description;
    private String petId = "1";
    private String practitionerId;
/*    private int petId;
    private int practitionerId;*/
    private boolean status;

    public VisitRequestDTO(LocalDateTime now, String description, String petId, String practitionerId) {
        this.visitDate = LocalDateTime.parse(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        this.description = description;
        this.petId =  petId;
        this.practitionerId = practitionerId;
        this.status = true;
    }
}
