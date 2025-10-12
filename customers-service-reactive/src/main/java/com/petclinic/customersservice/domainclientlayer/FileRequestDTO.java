package com.petclinic.customersservice.domainclientlayer;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileRequestDTO {

    private String fileId;
    private String fileName;
    private String fileType;
    private byte[] fileData;
}
