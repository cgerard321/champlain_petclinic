package com.petclinic.bffapigateway.dtos.Pets;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PetRequestDTO {

    private String ownerId;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;
    private String petTypeId;
    //private String photoId;
    private String isActive;
    private String weight;
}
