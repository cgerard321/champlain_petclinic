package com.petclinic.products.datalayer.ratings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("review_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSettings {
    @Id
    private Long id;
    private Integer editPeriodHours;
}
