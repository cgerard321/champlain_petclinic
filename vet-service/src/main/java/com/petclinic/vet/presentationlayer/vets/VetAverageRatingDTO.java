package com.petclinic.vet.presentationlayer.vets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetAverageRatingDTO {

    private VetResponseDTO vetDTO;
    private String vetId;
    private double averageRating;


}