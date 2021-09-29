package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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


    public Mono<UserDetails> getUser(final long userId) {
        return webClientBuilder.build().get()
                .uri(authServiceUrl + "/{userId}", userId)
                .retrieve()
                .bodyToMono(UserDetails.class);
    }

    public Flux<UserDetails> getUsers() {
        return webClientBuilder.build().get()
                .uri(authServiceUrl)
                .retrieve()
                .bodyToFlux(UserDetails.class);
    }

    public Mono<UserDetails> createUser (final UserDetails model) {
        return webClientBuilder.build().post()
                .uri(authServiceUrl + model)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(UserDetails.class);
    }

    public Flux<UserDetails> createUsers (){
        return webClientBuilder.build().post()
                .uri(authServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToFlux(UserDetails.class);
    }

    public Mono<UserDetails> updateUser (final int userId, final UserDetails model){
        return webClientBuilder.build().put()
                .uri(authServiceUrl + userId + model)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(UserDetails.class);
    }

    public Mono<UserDetails> deleteUser(final long userId) {
        return webClientBuilder.build()
                .delete()
                .uri(authServiceUrl + "/{userId}", userId)
                .retrieve()
                .bodyToMono(UserDetails.class);
    }
}

