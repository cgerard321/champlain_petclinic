package com.petclinic.bffapigateway.dtos.CustomerDTOs;

import lombok.*;

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
}
