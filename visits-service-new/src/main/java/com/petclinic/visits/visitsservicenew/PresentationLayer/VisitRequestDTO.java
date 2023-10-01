package com.petclinic.visits.visitsservicenew.PresentationLayer;


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
/*    private int year;
    private int month;
    private int day;*/
    private String description;
    private String petId;
    private String practitionerId;
    private boolean status;
}
