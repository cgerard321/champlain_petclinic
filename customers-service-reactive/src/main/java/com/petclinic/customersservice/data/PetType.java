package com.petclinic.customersservice.data;

import lombok.*;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetType {

    @Id
    private Integer id;
    private String name;

}
