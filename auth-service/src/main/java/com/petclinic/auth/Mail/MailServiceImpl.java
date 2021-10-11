package com.petclinic.auth.Mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import retrofit2.Response;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final MailServiceCall mailServiceCall;

    @Override
    public String sendMail(Mail mail) {
        try {
            Response<String> execute = mailServiceCall.sendMail(mail).execute();
            if(execute.code() == 400) {
                log.error(execute.message());
                log.error(execute.errorBody().string());
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, execute.errorBody().string());
            }
            log.info("Mail service returned {} status code", execute.code());
            return execute.body();
        } catch (IOException e) {
            log.error(e.toString());
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to send mail");
        }
    }
}
