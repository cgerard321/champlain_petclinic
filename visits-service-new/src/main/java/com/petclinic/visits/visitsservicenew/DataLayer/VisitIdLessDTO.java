package com.petclinic.visits.visitsservicenew.DataLayer;


import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class VisitIdLessDTO {
    private Date date;
    private String description;
    private int petId;
    private int practitionerId;
    private boolean status;
}
