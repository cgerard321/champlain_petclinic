package com.petclinic.bffapigateway.dtos.Pets;

import com.petclinic.bffapigateway.dtos.Vets.VisitDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PetResponseDTO {

    private int id;

    private String name;

    private String birthDate;

    private PetType type;

    private int imageId;

    //private final List<VisitDetails> visits = new ArrayList<>();

}

