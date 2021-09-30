package com.petclinic.auth;


import com.petclinic.auth.Mail.Mail;
import com.petclinic.auth.Mail.MailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class AuthMailServiceTests {

    @Autowired
    private MailService mailService;

    private final Mail
            EMAIL_VALID = new Mail("to@test.com", "test-subject", "test-message"),
            EMAIL_INVALID = new Mail();

    @Test
    void loads(){}

    @Test
    @DisplayName("Send valid email")
    void send_valid_email() {
        assertEquals("Message sent to " + EMAIL_VALID.getTo(), mailService.sendMail(EMAIL_VALID));
    }

    @Test
    @DisplayName("Send invalid email")
    void send_invalid_email() {
        assertThrows(HttpClientErrorException.class, () -> mailService.sendMail(EMAIL_INVALID));
    }
}
