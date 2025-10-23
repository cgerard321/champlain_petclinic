package com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileRequestDTO {
    
    private String fileName;
    private String fileType;
    private byte[] fileData;
}
