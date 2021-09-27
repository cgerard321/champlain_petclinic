package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String authServiceUrl;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.auth-service.host}") String authServiceHost,
            @Value("${app.auth-service.port}") String authServicePort
    ) {
        this.webClientBuilder = webClientBuilder;
        authServiceUrl = "http://" + authServiceHost + ":" + authServicePort + "/users";
    }

    public Mono<UserDetails> getUser(final long userId){
        return webClientBuilder.build().get().uri(authServiceUrl + "/{userId}", userId).retrieve().bodyToMono(UserDetails.class);
    }

    public Flux<UserDetails> getUsers() {
        return webClientBuilder.build().get()
                .uri(authServiceUrl)
                .retrieve()
                .bodyToFlux(UserDetails.class);
    }
}
