package com.petclinic.visits.visitsservicenew.DomainClientLayer.SimpleJavaMail;


import lombok.NoArgsConstructor;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;


@NoArgsConstructor
public class SimpleJavaMailClient {


    private final Mailer mailer = MailerBuilder
            .withSMTPServer("smtp.gmail.com", 587, "champlain.petclinic@gmail.com", System.getenv("SMTP_PASS")).withTransportStrategy(TransportStrategy.SMTP_TLS)
//            .withEmailOverrides(EmailBuilder.startingBlank().from("From", "champlain.petclinic@gmail.com").buildEmailCompletedWithDefaultsAndOverrides())
            .buildMailer();


    public void sendMail(Email email){
        mailer.sendMail(email);
    }

}
