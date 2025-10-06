package com.petclinic.vet.presentationlayer.education;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationResponseDTO {
    private String educationId;
    private String vetId;
    private String schoolName;
    private String degree;
    private String fieldOfStudy;
    private String startDate;
    private String endDate;
}
