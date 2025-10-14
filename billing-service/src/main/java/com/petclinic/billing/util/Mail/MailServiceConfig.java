package com.petclinic.billing.util.Mail;

import com.petclinic.billing.domainclientlayer.Mailing.MailServiceCall;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static java.lang.String.format;

@Configuration
public class MailServiceConfig {

    private final String MAIL_BASE_URL;

    public MailServiceConfig(
            @Value("${app.mailer-service.host}") String mailURL,
            @Value("${app.mailer-service.port}") String mailPORT
    ) {
        MAIL_BASE_URL = format("http://%s:%s", mailURL, mailPORT);
    }

    @Bean
    public MailServiceCall getMailerServiceCall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MAIL_BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(MailServiceCall.class);
    }
}
