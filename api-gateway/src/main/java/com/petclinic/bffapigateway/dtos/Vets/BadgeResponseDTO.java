package com.petclinic.bffapigateway.dtos.Vets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BadgeResponseDTO {
    private String vetId;
    private String badgeDate;
    private BadgeTitle badgeTitle;
    private String resourceBase64;
}
