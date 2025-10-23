package com.petclinic.vet.presentationlayer.files;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileResponseDTO {

    private String fileId;
    private String fileName;
    private String fileType;
    private byte[] fileData;
}
