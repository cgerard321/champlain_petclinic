package com.petclinic.customersservice.data;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetType {

    @Id
    private String id;
    private String petTypeId;
    private String name;
    private String petTypeDescription;

}
