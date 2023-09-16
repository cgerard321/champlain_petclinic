package com.petclinic.bffapigateway.dtos.Owners;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petclinic.bffapigateway.dtos.Pets.PetDetails;
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
public class OwnerDetails {

    private int id;

    private String firstName;

    private String lastName;

    private String address;

    private String city;

    private String telephone;

    private int imageId;

    private final List<PetDetails> pets = new ArrayList<>();

    @JsonIgnore
    public List<Integer> getPetIds() {
        return pets.stream()
                .map(PetDetails::getId)
                .collect(toList());
    }
}
