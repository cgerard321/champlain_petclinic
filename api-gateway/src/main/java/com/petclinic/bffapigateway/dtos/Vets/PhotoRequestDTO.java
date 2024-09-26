package com.petclinic.bffapigateway.dtos.Vets;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoRequestDTO {
    private String vetId;
    private String filename;
    private String imgType;
    private String resourceBase64;
    private String imgBase64;
}
