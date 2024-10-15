package com.petclinic.vet.servicelayer.ratings;

import com.petclinic.vet.dataaccesslayer.ratings.PredefinedDescription;
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
    private Double rating;
    private String rateDescription;
    private PredefinedDescription experience;
    private String rateDate;
    private String customerName;
}