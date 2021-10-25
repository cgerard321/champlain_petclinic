package com.petclinic.visits.datalayer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VisitDTO {
    private String visitId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private String description;
    private int petId;
    private int practitionerId;
    private boolean status;

}
