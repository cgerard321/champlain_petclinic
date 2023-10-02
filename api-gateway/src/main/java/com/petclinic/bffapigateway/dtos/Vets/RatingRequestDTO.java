package com.petclinic.bffapigateway.dtos.Vets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingRequestDTO {
    private String vetId;
    private Double rateScore;
    private String rateDescription;
    private String rateDate;
    private PredefinedDescription predefinedDescription;
}
