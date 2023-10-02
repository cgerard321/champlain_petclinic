package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.Vet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetAverageRatingDTO {

    private VetDTO vetDTO;
    private String vetId;
    private double averageRating;


}