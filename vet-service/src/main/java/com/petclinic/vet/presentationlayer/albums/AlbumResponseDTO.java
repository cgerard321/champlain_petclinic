package com.petclinic.vet.presentationlayer.albums;


import lombok.*;

import java.util.List;

import com.petclinic.vet.presentationlayer.photos.PhotoResponseDTO;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponseDTO {
    private String vetId;
    private List<PhotoResponseDTO> photos;
}
