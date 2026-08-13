package com.petclinic.customersservice.data;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Pet {

    @Id
    private String id;
    private String petId; // public id
    private String ownerId;
    private String name;
    private Date birthDate;
    private String petTypeId;
    private String photoId;
    private String isActive; //Should be a boolean
    private String weight; //should be a double

}
