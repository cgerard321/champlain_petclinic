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
public class VisitRequestDTO {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime visitDate;
    private String description;
    private String petId;
    private String practitionerId;
    private Status status;

    public VisitRequestDTO(LocalDateTime now, String description, String petId, String practitionerId) {
        this.visitDate = now;
        this.description = description;
        this.petId =  petId;
        this.practitionerId = practitionerId;
        this.status = Status.UPCOMING;
    }
}
