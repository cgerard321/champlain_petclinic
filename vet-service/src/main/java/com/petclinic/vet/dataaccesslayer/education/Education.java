package com.petclinic.vet.dataaccesslayer.education;

import lombok.*;
import org.springframework.data.annotation.Id;

@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Education {
    @Id //very important
    private String id;
    private String educationId;
    private String vetId;
    private String schoolName;
    private String degree;
    private String fieldOfStudy;
    private String startDate;
    private String endDate;
}

