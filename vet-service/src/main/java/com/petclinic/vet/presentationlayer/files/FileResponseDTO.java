package com.petclinic.vet.presentationlayer.files;

import lombok.*;
import java.util.Base64;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileResponseDTO {

    private String fileId;
    private String fileName;
    private String fileType;
    private String fileData; 
    
    public void setFileDataFromBytes(byte[] data) {
        this.fileData = Base64.getEncoder().encodeToString(data);
    }
    
    public byte[] getFileDataAsBytes() {
        if (fileData == null) return new byte[0];
        return Base64.getDecoder().decode(fileData);
    }
}
