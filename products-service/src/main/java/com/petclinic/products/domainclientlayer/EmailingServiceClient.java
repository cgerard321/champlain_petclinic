package com.petclinic.products.domainclientlayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class EmailingServiceClient {
    private final WebClient client;
    private final String baseEmailingURL;

    public EmailingServiceClient(
            @Value("${app.emailing-service.host}") String emailingServiceHost,
            @Value("${app.emailing-service.port}") String emailingServicePort
    ){
        this.baseEmailingURL = "http://" + emailingServiceHost + ":" + emailingServicePort + "/email";
        this.client = WebClient.builder().baseUrl(baseEmailingURL).build();
    }

    public Mono<HttpStatusCode> sendEmail(EmailRequestModel requestModel){
        return client.post()
                .uri(baseEmailingURL + "/send")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchangeToMono(response -> Mono.just(response.statusCode()));
    }

}
