package com.petclinic.vet.dataaccesslayer.ratings;

import lombok.*;
import org.springframework.data.annotation.Id;

@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Rating {
    @Id
    private String ratingId;
    private String vetId;
    private Double rating;
    private String rateDescription;
    private PredefinedDescription experience;
    private String rateDate;

}
