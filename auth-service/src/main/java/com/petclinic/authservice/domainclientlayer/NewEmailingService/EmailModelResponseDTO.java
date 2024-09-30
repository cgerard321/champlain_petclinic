package com.petclinic.authservice.domainclientlayer.NewEmailingService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailModelResponseDTO {
    private int Id;
    private String Email;
    private String Subject;
    private String Body;
    private String EmailStatus;
}
