package com.petclinic.bffapigateway.dtos.CustomerDTOs;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileRequestDTO {
    private String fileName;
    private String fileType;
    private String fileData;
}
