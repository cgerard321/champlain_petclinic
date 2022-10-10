package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.Photo;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetDTO {

    private int id;
    private int ownerId;
    private String name;
    private Date birthDate;
    private PetType petType;
    private Photo photo;

}
