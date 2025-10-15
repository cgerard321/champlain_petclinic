package com.petclinic.cartsservice.domainclientlayer.Auth;

import com.petclinic.cartsservice.domainclientlayer.Auth.Rethrower;
import com.petclinic.cartsservice.domainclientlayer.Auth.UserDetails;
import com.petclinic.cartsservice.utils.exceptions.GenericHttpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Component
public class AuthServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String authServiceUrl;
    @Autowired
    private Rethrower rethrower;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.auth-service.host}") String authServiceHost,
            @Value("${app.auth-service.port}") String authServicePort) {
        this.webClientBuilder = webClientBuilder;
        authServiceUrl = "http://" + authServiceHost + ":" + authServicePort;
    }

    public Mono<UserDetails> getUserById(String jwtToken, String userId) {
        return webClientBuilder.build()
                .get()
                .uri(authServiceUrl + "/users/{userId}", userId)
                .cookie("Bearer", jwtToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        n -> rethrower.rethrow(n,
                                x -> new GenericHttpException(x.get("message").toString(), NOT_FOUND))
                )
                .bodyToMono(UserDetails.class);
    }
}

