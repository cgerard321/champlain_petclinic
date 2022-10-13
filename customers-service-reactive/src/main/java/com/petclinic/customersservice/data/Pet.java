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
    private int id;
    private int ownerId;
    private int petTypeId;
    private String name;
    private Date birthDate;
    private String type;
    private Photo photo;
    private PetType petType;

}
