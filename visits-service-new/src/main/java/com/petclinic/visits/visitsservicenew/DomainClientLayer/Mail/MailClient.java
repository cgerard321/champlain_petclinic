package com.petclinic.visits.visitsservicenew.DomainClientLayer.Mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MailClient {
    private final WebClient webClient;
    public MailClient(@Value("${app.mailer-service.host}") String mailURL, @Value("${app.mailer-service.port}") String mailPORT) {
        this.webClient = WebClient.builder().baseUrl("http://"+mailURL+":"+mailPORT+"/mail").build();
    }

    public Mono<String> sendEmail(Mail mail) {
//    public String sendEmail(Mail mail) {
        return webClient
                .post()
                .uri("")
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(mail)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
//                    HttpStatusCode statusCode = error.statusCode();
//                    if (Objects.equals(statusCode, HttpStatus.NOT_FOUND))
//                        return Mono.error(new NotFoundException("Problem with mailer"));
                    /*
                    try {
            Response<String> execute = mailServiceCall.sendMail(mail).execute();
            if (execute.code() == 400) {
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
                     */
                    return Mono.error(new IllegalArgumentException("Problem with mailer"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Problem with mailer"))
                ).bodyToMono(String.class)
                ;
    }
}
