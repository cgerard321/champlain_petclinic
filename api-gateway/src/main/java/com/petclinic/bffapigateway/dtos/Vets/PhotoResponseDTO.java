package com.petclinic.vet.presentationlayer;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponseDTO {
    private String vetId;
    private String filename;
    private String imgType;
    private String resourceBase64;
}
