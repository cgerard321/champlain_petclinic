package com.petclinic.visits.visitsservicenew.DataLayer;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitDTO {

    private String visitId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private String description;
    private int petId;
    private int practitionerId;
    private boolean status;
}
