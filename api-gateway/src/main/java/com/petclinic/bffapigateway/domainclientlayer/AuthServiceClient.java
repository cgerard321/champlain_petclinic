package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Register;
import com.petclinic.bffapigateway.dtos.UserDetails;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.utils.Rethrower;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static reactor.core.publisher.Mono.just;

@Component
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String authServiceUrl;

    @Autowired
    private Rethrower rethrower;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.auth-service.host}") String authServiceHost,
            @Value("${app.auth-service.port}") String authServicePort
    ) {
        this.webClientBuilder = webClientBuilder;
        authServiceUrl = "http://" + authServiceHost + ":" + authServicePort;
    }


    public Mono<UserDetails> getUser(final long userId) {
        return webClientBuilder.build().get()
                .uri(authServiceUrl + "/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserDetails.class);
    }

    public Flux<UserDetails> getUsers() {
        return webClientBuilder.build().get()
                .uri(authServiceUrl + "/users")
                .retrieve()
                .bodyToFlux(UserDetails.class);
    }

    public Mono<UserDetails> createUser (final Register model) {
        return webClientBuilder.build().post()
                .uri(authServiceUrl + "/users")
                .body(just(model), Register.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        n -> rethrower.rethrow(n,
                                x -> new GenericHttpException(x.get("message").toString(), BAD_REQUEST))
                        )
                .bodyToMono(UserDetails.class);
    }

    public Flux<UserDetails> createUsers (){
        return webClientBuilder.build().post()
                .uri(authServiceUrl + "/users")
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
                .uri(authServiceUrl + "/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserDetails.class);
    }

    public Mono<UserDetails> verifyUser(final String token) {
        return webClientBuilder.build()
                .get()
                .uri(authServiceUrl + "/users/" + token)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        n -> rethrower.rethrow(n,
                                x -> new GenericHttpException(x.get("message").toString(), BAD_REQUEST))
                )
                .bodyToMono(UserDetails.class);
    }
}

