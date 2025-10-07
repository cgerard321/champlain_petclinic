package com.petclinic.vet.presentationlayer.photos;

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
    private byte[] data;
}