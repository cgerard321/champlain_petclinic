package com.petclinic.products.domainclientlayer;

import com.petclinic.products.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {
    private final WebClient client;
    private final String baseUserURL;

    public UserServiceClient(
            @Value("${app.auth-service.host}") String userServiceHost,
            @Value("${app.auth-service.port}") String userServicePort
    ){
        this.baseUserURL = "http://" + userServiceHost + ":" + userServicePort + "/users";
        this.client = WebClient.builder().baseUrl(baseUserURL).build();
    }

    public Mono<UserDetails> getUserByUserId(String userId){
        return client.get()
                .uri(baseUserURL + "/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                // If you want this to work, you'll either need to generate a JWT token or modify the way auth-service works.
                // Email field in notification is unwanted and we'd rather use the email assigned to user :/
                .cookie("Bearer", "")
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        error -> switch (error.statusCode().value()){
                            case 404 -> Mono.error(new NotFoundException("No user with userId: " + userId));
                            default -> Mono.error(new RuntimeException("Unknown error: " + error.statusCode().value()));
                        }
                )
                .bodyToMono(UserDetails.class);
    }
}
