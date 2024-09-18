package com.petclinic.bffapigateway.dtos.Vets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EducationRequestDTO {
    private String schoolName;
    private String degree;
    private String fieldOfStudy;
    private String startDate;
    private String endDate;
}
