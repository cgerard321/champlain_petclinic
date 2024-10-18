package com.petclinic.bffapigateway.dtos.Visits.reviews;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private String reviewId;
    private String ownerId;
    private int rating;
    private String reviewerName;
    private String review;
    private LocalDateTime dateSubmitted;
}
