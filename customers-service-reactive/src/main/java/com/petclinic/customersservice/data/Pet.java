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
    private String Id;
    private String ownerId;
    private String name;
    private Date birthDate;
    private String petTypeId;
    private String photoId;
}
