package com.petclinic.bffapigateway.dtos.Emailing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEmailModelRequestDTO {
    private String EmailToSendTo;
    private String EmailTitle;
    private String TemplateName;
    private String Header ;
    private String Body;
    private String Footer ;
    private String CorrespondantName ;
    private String SenderName ;
    private LocalDateTime SentDate;
}
