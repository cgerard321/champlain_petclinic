package com.petclinic.bffapigateway.dtos.Products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDTO {

    private String imageId;
    private String imageName;
    private String imageType;
    private byte[] imageData;
}