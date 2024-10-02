package com.petclinic.authservice.domainclientlayer.NewEmailingService;


import com.petclinic.authservice.domainclientlayer.NewEmailingService.DirectEmailModelRequestDTO;
import com.petclinic.authservice.domainclientlayer.NewEmailingService.EmailModelResponseDTO;
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
                                 @Value("${emailing-service.host}") String emailingServiceHost,
                                 @Value("${emailing-service.port}") String emailingServicePort) {
        this.webClientBuilder = webClientBuilder;
        emailingServiceUrl = "http://" + emailingServiceHost + ":" + emailingServicePort + "/email";
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
