package com.petclinic.vet.servicelayer.education;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EducationRequestDTO {
    private String vetId;
    private String schoolName;
    private String degree;
    private String fieldOfStudy;
    private String startDate;
    private String endDate;
}
