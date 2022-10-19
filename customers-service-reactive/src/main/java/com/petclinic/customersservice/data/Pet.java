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
    private int petId;
    private int ownerId;
    private int petTypeId;
    private String name;
    private Date birthDate;
    private int photoId;
}
