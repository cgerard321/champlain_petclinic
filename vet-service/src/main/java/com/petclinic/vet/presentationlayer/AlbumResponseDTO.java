package com.petclinic.vet.presentationlayer;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponseDTO {
    private String vetId;
    private String title;
    private List<PhotoResponseDTO> photos;
}
