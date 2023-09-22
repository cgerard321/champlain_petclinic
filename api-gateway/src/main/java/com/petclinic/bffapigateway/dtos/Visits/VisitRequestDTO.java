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

    private LocalDateTime visitDate;
/*   private int year;
    private int month;
    private int day;*/
=======
   // private LocalDateTime visitDate;
   private int year;
    private int month;
    private int day;
>>>>>>> 4237d248 (Working on Post)
    private String description;
    private String petId;
    private String practitionerId;
/*    private int petId;
    private int practitionerId;*/
    private boolean status;
}
