package com.petclinic.vet.presentationlayer.photos;

import lombok.*;

import org.springframework.core.io.Resource;

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
    private byte[] resource;
}
