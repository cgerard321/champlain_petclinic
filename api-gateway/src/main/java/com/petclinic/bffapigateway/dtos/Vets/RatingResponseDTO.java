package com.petclinic.bffapigateway.dtos.Vets;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponseDTO {
    private String ratingId;
    private String vetId;
    private Double rateScore;
    private String rateDescription;
    private PredefinedDescription predefinedDescription;
    private String rateDate;
}