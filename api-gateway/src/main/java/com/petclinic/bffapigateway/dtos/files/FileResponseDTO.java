package com.petclinic.bffapigateway.dtos.files;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponseDTO {
    private String fileId;
    private String fileName;
    private String fileType;
    private byte[] fileData;
}
