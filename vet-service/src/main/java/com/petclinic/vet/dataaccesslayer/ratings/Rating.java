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
    private String id;
    private String ratingId;
    private String vetId;
    private Double rateScore;
    private String rateDescription;
    private PredefinedDescription predefinedDescription;
    private String rateDate;
}
