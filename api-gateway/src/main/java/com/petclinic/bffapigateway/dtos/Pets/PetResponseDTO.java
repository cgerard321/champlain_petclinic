package com.petclinic.bffapigateway.dtos.Pets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PetResponseDTO {

    private String petId;

    private String name;

    private String birthDate;

    private PetType type;

    private int imageId;

    //private final List<VisitDetails> visits = new ArrayList<>();

}

