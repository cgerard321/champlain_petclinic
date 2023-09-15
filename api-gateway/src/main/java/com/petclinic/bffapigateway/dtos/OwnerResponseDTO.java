package com.petclinic.bffapigateway.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OwnerResponseDTO {

    private String ownerId;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;
    //private int imageId;

    /*private final List<PetResponseDTO> pets = new ArrayList<>();

    @JsonIgnore
    public List<Integer> getPetIds() {
        return pets.stream()
                .map(PetResponseDTO::getId)
                .collect(toList());
    }*/
}
