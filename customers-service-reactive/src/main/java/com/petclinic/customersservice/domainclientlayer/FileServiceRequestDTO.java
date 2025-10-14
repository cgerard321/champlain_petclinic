package com.petclinic.customersservice.domainclientlayer;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileServiceRequestDTO {
    
    private String fileName;
    private String fileType;
    private String fileData;
}