package com.petclinic.products.presentationlayer.ratings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponseModel {
    private Byte rating;
    private String review;
}
