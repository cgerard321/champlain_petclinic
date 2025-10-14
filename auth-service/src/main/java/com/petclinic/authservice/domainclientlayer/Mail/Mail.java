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
public class Mail {
    private String emailSendTo;
    private String emailTitle;
    private String templateName;
    private String header;
    private String body;
    private String footer;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String correspondantName;
    private String senderName;
}
