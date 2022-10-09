package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Photo;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDTO {

    private int id;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;
    private Photo photo;

}
