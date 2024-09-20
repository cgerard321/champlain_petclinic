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
    private String ownerId;
    private String jwtToken;//used to get the userDetails from the Auth-Service when sending visit emails
    private String practitionerId;
    private Status status;
    private String visitId;

    public VisitRequestDTO(LocalDateTime now, String description, String petId, String ownerId, String jwtToken, String practitionerId, String visitId) {
        this.visitDate = now;
        this.description = description;
        this.petId =  petId;
        this.ownerId = ownerId;
        this.jwtToken = jwtToken;
        this.practitionerId = practitionerId;
        this.status = Status.UPCOMING;
        this.visitId = visitId;
    }
}
