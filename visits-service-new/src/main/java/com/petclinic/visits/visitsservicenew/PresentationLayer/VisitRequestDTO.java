package com.petclinic.visits.visitsservicenew.PresentationLayer;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
//Finished
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
    private boolean reminder;
    private String ownerEmail;
}
