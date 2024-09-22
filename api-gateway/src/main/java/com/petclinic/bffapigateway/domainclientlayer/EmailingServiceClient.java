package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Emailing.DirectEmailModelRequestDTO;
import com.petclinic.bffapigateway.dtos.Emailing.EmailModelResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.exceptions.InventoryNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;

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
                .bodyValue(htmlContent) // Send HTML content in the body
                .retrieve()
                .bodyToMono(String.class)
                .switchIfEmpty(Mono.error(new RuntimeException("No response from service")));
    }
    public Mono<String> sendEmail(DirectEmailModelRequestDTO directEmailModelRequestDTO) {
        return webClientBuilder.build().post()
                .uri(emailingServiceUrl + "/send")
                .bodyValue(directEmailModelRequestDTO) // Send HTML content in the body
                .retrieve()
                .bodyToMono(String.class)
                .switchIfEmpty(Mono.error(new RuntimeException("No response from service")));
    }




}
