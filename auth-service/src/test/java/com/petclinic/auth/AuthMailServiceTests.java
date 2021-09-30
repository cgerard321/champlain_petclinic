package com.petclinic.auth;


import com.petclinic.auth.Mail.Mail;
import com.petclinic.auth.Mail.MailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class AuthMailServiceTests {

    @Autowired
    private MailService mailService;

    private final Mail EMAIL_VALID = new Mail("to@test.com", "test-subject", "test-message");

    @Test
    void loads(){}

    @Test
    @DisplayName("Send valid email")
    void send_valid_email() {
        assertEquals("Message sent to " + EMAIL_VALID.getTo(), mailService.sendMail(EMAIL_VALID));
    }
}
