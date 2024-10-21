package com.petclinic.bffapigateway.dtos.Emailing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedEmailResponseDTO {
    public String from ;      // The email address of the sender
    public String subject ;  // The subject line of the email
    public Date dateReceived ;  // The date and time the email was sent
    public String plainTextBody ; // The main text content of the email
}
