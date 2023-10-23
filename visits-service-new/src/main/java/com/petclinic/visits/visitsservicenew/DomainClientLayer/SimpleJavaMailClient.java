package com.petclinic.visits.visitsservicenew.DomainClientLayer;


import lombok.NoArgsConstructor;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;


@NoArgsConstructor
public class SimpleJavaMailClient {
    private final Email email = EmailBuilder.startingBlank()
            .from("From", "champlain.petclinic@gmail.com")
            .to("William", "william.chalifoux@gmail.com")
            .withSubject("Email test with simple java mail")
            .withPlainText("Email Body")
            .buildEmail();

    private final Mailer mailer = MailerBuilder
            .withSMTPServer("smtp.gmail.com", 587, "champlain.petclinic@gmail.com", System.getenv("SMTP_PASS")).withTransportStrategy(TransportStrategy.SMTP_TLS)
//            .withEmailOverrides(EmailBuilder.startingBlank().from("From", "champlain.petclinic@gmail.com").buildEmailCompletedWithDefaultsAndOverrides())
            .buildMailer();


    public void sendMail(){
        /*Email email*/
        mailer.sendMail(email);
    }

}
