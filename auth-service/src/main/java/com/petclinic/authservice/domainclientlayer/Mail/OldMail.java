package com.petclinic.authservice.domainclientlayer.Mail;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class OldMail {

    private String
            to,
            subject,
            message;
}
