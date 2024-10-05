package com.petclinic.products.presentationlayer.images;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequestModel {

    private String imageName;
    private String imageType;
    private byte[] imageData;
}
