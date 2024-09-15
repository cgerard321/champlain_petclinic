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
    private LocalDateTime visitStartDate;
    private String description;
    private String petId;
    private String ownerId;
    private String jwtToken;//used to get the userDetails from the Auth-Service when sending visit emails
    private String practitionerId;
    private Status status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime visitEndDate;

    public VisitRequestDTO(LocalDateTime now, String description, String petId, String ownerId, String jwtToken, String practitionerId) {
        this.visitStartDate = now;
        this.description = description;
        this.petId =  petId;
        this.ownerId = ownerId;
        this.jwtToken = jwtToken;
        this.practitionerId = practitionerId;
        this.status = Status.UPCOMING;
    }
}
