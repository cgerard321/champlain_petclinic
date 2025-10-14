package com.petclinic.visits.visitsservicenew.BusinessLayer;


import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing.Mail;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing.MailService;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing.MailServiceCall;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.HttpClientErrorException;
import retrofit2.Response;
import retrofit2.mock.Calls;

import java.io.IOException;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest()
public class MailServiceTests {
    @Autowired
    private MailService mailService;

    @MockBean
    private MailServiceCall mockMailCall;

    private final Mail
            EMAIL_VALID = new Mail("test@gmail.com", "Verification Email", "Default", "Pet clinic - Verification Email",
            "valid email body",
            "Test Footer", "test_correspondant_name", "ChamplainPetClinic@gmail.com"),
            EMAIL_EMPTY_INVALID = new Mail();
    public final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @BeforeEach
    void setUp(){
        when(mockMailCall.sendMail(EMAIL_VALID)).thenReturn(Calls.response(format("Message sent to %s", EMAIL_VALID.getEmailSendTo())));
        when(mockMailCall.sendMail(EMAIL_EMPTY_INVALID)).thenReturn(Calls.response(Response.error(400, ResponseBody.create("Bad request", JSON))));
    }

    @Test
    void loads(){}

    @Test
    @DisplayName("Send valid email")
    void send_valid_email() {
        assertEquals("Message sent to " + EMAIL_VALID.getEmailSendTo(), mailService.sendMail(EMAIL_VALID));
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
        when(mockMailCall.sendMail(EMAIL_EMPTY_INVALID)).thenReturn(Calls.failure(new IOException()));
        HttpClientErrorException httpClientErrorException =
                assertThrows(HttpClientErrorException.class, () -> mailService.sendMail(EMAIL_EMPTY_INVALID));
        assertEquals("500 Unable to send mail", httpClientErrorException.getMessage());
    }


}
