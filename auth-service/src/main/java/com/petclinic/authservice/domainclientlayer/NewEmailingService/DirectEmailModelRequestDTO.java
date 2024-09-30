package com.petclinic.authservice.domainclientlayer.NewEmailingService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectEmailModelRequestDTO {
    private String EmailToSendTo;
    private String EmailTitle;
    private String TemplateName;
    private String Header ;
    private String Body;
    private String Footer ;
    private String CorrespondantName ;
    private String SenderName ;
}
