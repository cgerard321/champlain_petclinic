package com.petclinic.auth;


import com.petclinic.auth.Mail.Mail;
import com.petclinic.auth.Mail.MailService;
import com.petclinic.auth.Mail.MailServiceCall;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import retrofit2.Response;
import retrofit2.mock.Calls;

import java.io.IOException;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
            EMAIL_EMPTY_INVALID = new Mail();
    public final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @BeforeEach
    void setUp(){

        when(mockMailCall.sendMail(EMAIL_VALID))
                .thenReturn(Calls.response(format("Message sent to %s", EMAIL_VALID.getTo())));

        when(mockMailCall.sendMail(EMAIL_EMPTY_INVALID))
                .thenReturn(Calls.response(Response.error(400, ResponseBody.create(JSON, "Bad request"))));
    }

    @Test
    void loads(){}

    @Test
    @DisplayName("Send valid email")
    void send_valid_email() {
        assertEquals("Message sent to " + EMAIL_VALID.getTo(), mailService.sendMail(EMAIL_VALID));
    }

    @Test
    @DisplayName("Send invalid empty email")
    void send_invalid_empty_email() {
        HttpClientErrorException httpClientErrorException =
                assertThrows(HttpClientErrorException.class, () -> mailService.sendMail(EMAIL_EMPTY_INVALID));
        assertEquals("400 Bad Request", httpClientErrorException.getMessage());
    }

    @Test
    @DisplayName("IOException graceful handling")
    void io_exception_graceful_handling() {
        when(mockMailCall.sendMail(EMAIL_EMPTY_INVALID))
                .thenReturn(Calls.failure(new IOException()));
        HttpClientErrorException httpClientErrorException =
                assertThrows(HttpClientErrorException.class, () -> mailService.sendMail(EMAIL_EMPTY_INVALID));
        assertEquals("500 Unable to send mail", httpClientErrorException.getMessage());
    }
}
