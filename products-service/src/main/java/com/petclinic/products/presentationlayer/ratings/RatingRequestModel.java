package com.petclinic.products.presentationlayer.ratings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequestModel {
    private Byte rating;
    private String review;
    private Byte qualityRating;
    private Byte valueRating;
    private Byte petSatisfactionRating;
    private Byte ingredientsRating;
}
