package com.petclinic.auth.Config;

import com.petclinic.auth.Mail.MailServiceCall;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;

import static java.lang.String.format;

@Configuration
public class MailServiceConfig {

    private final String MAIL_BASE_URL;

    public MailServiceConfig(
            @Value("") String mailURL,
            @Value("") String mailPORT
    ) {
        MAIL_BASE_URL = format("%s:%s", mailURL, mailPORT);
    }

    @Bean
    public MailServiceCall getMailerServiceCall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MAIL_BASE_URL)
                .build();
        return retrofit.create(MailServiceCall.class);
    }
}
