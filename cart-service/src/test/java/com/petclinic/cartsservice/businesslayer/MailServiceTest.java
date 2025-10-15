package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.domainclientlayer.Mailing.Mail;
import com.petclinic.cartsservice.domainclientlayer.Mailing.MailServiceCall;
import com.petclinic.cartsservice.domainclientlayer.Mailing.MailServiceImpl;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import retrofit2.Response;
import retrofit2.mock.Calls;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

    @Mock
    private MailServiceCall mockMailCall;

    @InjectMocks
    private MailServiceImpl mailService;

    private final Mail
            EMAIL_VALID = new Mail("test@gmail.com", "Verification Email", "Default", "Pet clinic - Verification Email",
            "valid email body",
            "Test Footer", "test_correspondant_name", "ChamplainPetClinic@gmail.com"),
            EMAIL_EMPTY_INVALID = new Mail();

    public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Test
    void loads(){}

    @Test
    @DisplayName("Send valid email")
    void send_valid_email() {
        when(mockMailCall.sendMail(EMAIL_VALID))
                .thenReturn(Calls.response("Message sent to " + EMAIL_VALID.getEmailSendTo()));
        assertEquals("Message sent to " + EMAIL_VALID.getEmailSendTo(), mailService.sendMail(EMAIL_VALID));
    }

    @Test
    @DisplayName("Send invalid empty email")
    void send_invalid_empty_email() {
        when(mockMailCall.sendMail(EMAIL_EMPTY_INVALID))
                .thenReturn(Calls.response(Response.error(400, ResponseBody.create("Bad request", JSON))));
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