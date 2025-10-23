package com.petclinic.vet.presentationlayer.files;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Base64;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileRequestDTO {

    @JsonProperty("fileName")
    private String fileName;
    
    @JsonProperty("fileType")
    private String fileType;
    
    @JsonProperty("fileData")
    private String fileData;  
    
    public void setFileDataFromBytes(byte[] bytes) {
        this.fileData = Base64.getEncoder().encodeToString(bytes);
    }
    
    public byte[] getFileDataAsBytes() {
        return fileData != null ? Base64.getDecoder().decode(fileData) : null;
    }
}