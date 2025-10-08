package com.petclinic.vet.presentationlayer.badges;

import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BadgeRequestDTO {
    private String vetId;
    private String badgeDate;
}
