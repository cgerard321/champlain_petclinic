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
<<<<<<< HEAD

=======
>>>>>>> 336d2006 (Working on parse date)
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
}
