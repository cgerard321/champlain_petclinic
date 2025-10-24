package com.petclinic.bffapigateway.dtos.Pets;

import com.petclinic.bffapigateway.dtos.Files.FileDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PetResponseDTO {

    private String ownerId;
    private String petId;
    private String name;
    private Date birthDate;
    private String petTypeId;
    private String photoId;
    private String isActive;
    private String weight;
    private FileDetails photo;
    //private final List<VisitDetails> visits = new ArrayList<>();

}

