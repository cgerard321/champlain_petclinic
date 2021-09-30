package com.petclinic.auth;


import com.petclinic.auth.Mail.Mail;
import com.petclinic.auth.Mail.MailService;
import com.petclinic.auth.Mail.MailServiceCall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import retrofit2.mock.Calls;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class AuthMailServiceTests {

    @Autowired
    private MailService mailService;

    @MockBean
    private MailServiceCall mockMailCall;

    private final Mail
            EMAIL_VALID = new Mail("to@test.com", "test-subject", "test-message"),
            EMAIL_INVALID = new Mail();

    @BeforeEach
    void setUp() {
        when(mockMailCall.sendMail(EMAIL_VALID))
                .thenReturn(Calls.response(format("Message sent to %s", EMAIL_VALID.getTo())));

        when(mockMailCall.sendMail(EMAIL_INVALID))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to send mail"));
    }

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
        HttpClientErrorException httpClientErrorException =
                assertThrows(HttpClientErrorException.class, () -> mailService.sendMail(EMAIL_INVALID));
        assertEquals("500 Unable to send mail", httpClientErrorException.getMessage());
    }
}
