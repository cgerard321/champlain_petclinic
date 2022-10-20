package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Photo;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class OwnerDTO {

    private String id;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;
    private String photoId;
    private Photo photo;
    private List<PetDTO> pets;

}
