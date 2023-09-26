package com.petclinic.billing.datalayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PetDTO {

    private String id;
    private String ownerId;
    private String name;
    private Date birthDate;
    private String petTypeId;
    private String photoId;
    private PetType petType;
    private Photo photo;

}
