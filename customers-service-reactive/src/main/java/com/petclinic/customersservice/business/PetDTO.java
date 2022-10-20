package com.petclinic.customersservice.business;


import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.Photo;
import lombok.*;

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
