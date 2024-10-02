package com.petclinic.bffapigateway.dtos.Ratings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequestModel {
    Byte rating;
    String review;
}
