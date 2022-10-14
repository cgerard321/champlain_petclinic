package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.Photo;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class OwnerAggregate {

    private int id;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;
    private Photo photo;
    private List<PetAggregate> pets;

}
