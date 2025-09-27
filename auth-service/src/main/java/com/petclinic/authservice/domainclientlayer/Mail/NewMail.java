package com.petclinic.authservice.domainclientlayer.Mail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewMail {
    private String EmailSendTo;
    private String EmailTitle;
    private String TemplateName;
    private String Header ;
    private String Body;
    private String Footer ;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String CorrespondantName ;
    private String SenderName ;
}
