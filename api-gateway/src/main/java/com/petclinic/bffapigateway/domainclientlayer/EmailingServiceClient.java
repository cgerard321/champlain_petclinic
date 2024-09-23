package com.petclinic.bffapigateway.domainclientlayer;


import com.petclinic.bffapigateway.dtos.Emailing.DirectEmailModelRequestDTO;
import com.petclinic.bffapigateway.dtos.Emailing.EmailModelResponseDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class EmailingServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String emailingServiceUrl;
    public EmailingServiceClient(WebClient.Builder webClientBuilder,
                                 @Value("${app.emailing-service.host}") String emailingServiceHost,
                                 @Value("${app.emailing-service.port}") String emailingServicePort) {
        this.webClientBuilder = webClientBuilder;
        emailingServiceUrl = "http://" + emailingServiceHost + ":" + emailingServicePort + "/email";
    }

    public Flux<EmailModelResponseDTO> getAllEmails(){
        return webClientBuilder.build().get()
                .uri(emailingServiceUrl + "/get")
                .retrieve()
                .bodyToFlux(EmailModelResponseDTO.class);
    }

    public Mono<String> addHtmlTemplate(String templateName, String htmlContent) {
        return webClientBuilder.build().post()
                .uri(emailingServiceUrl + "/templates/add/" + templateName)
                .contentType(MediaType.TEXT_HTML)  // Set Content-Type header to text/html
                .bodyValue(htmlContent)
                .retrieve()
                .bodyToMono(String.class)
                .switchIfEmpty(Mono.error(new RuntimeException("No response from service")));
    }

    public Mono<HttpStatus> sendEmail(DirectEmailModelRequestDTO directEmailModelRequestDTO) {
        return webClientBuilder.build().post()
                .uri(emailingServiceUrl + "/send")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(directEmailModelRequestDTO)
                .exchangeToMono(response -> {
                    // Return the status code directly
                    return Mono.just((HttpStatus) response.statusCode());
                });
    }

}
