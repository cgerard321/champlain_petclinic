package com.petclinic.customersservice.business;


import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.Photo;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class PetAggregate {

    private int id;
    private int ownerId;
    private int petTypeId;
    private String name;
    private Date birthDate;
    private PetType petType;
    private Photo photo;

}
