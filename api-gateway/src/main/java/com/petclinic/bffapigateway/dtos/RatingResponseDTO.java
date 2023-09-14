package com.petclinic.bffapigateway.dtos;

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
}