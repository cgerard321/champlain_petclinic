package com.petclinic.bffapigateway.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private String ownerId;

    private String firstName;

    private String lastName;

    private String address;

    private String city;

    private String telephone;

    private String photoId;

    private final List<PetDetails> pets = new ArrayList<>();

    @JsonIgnore
    public List<String> getPetIds() {
        return pets.stream()
                .map(PetDetails::getPetId)
                .collect(toList());
    }
}
