package com.petclinic.bffapigateway.dtos.Files;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileDetails {
    private String fileName;
    private String fileType;
    private byte[] fileData;
}
