package com.petclinic.bffapigateway.domainclientlayer;

<<<<<<< HEAD
import com.petclinic.bffapigateway.dtos.OwnerDetails;
=======
>>>>>>> 07aff82 (Implemented the delete user method, added testing.)
import com.petclinic.bffapigateway.dtos.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AuthServiceClient {
<<<<<<< HEAD
    private final WebClient.Builder webClientBuilder;
    private final String authenticationServiceUrl;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.authentication-service.host}") String authenticationServiceHost,
            @Value("${app.authentication-service.port}") String authenticationServicePort
    ) {
        this.webClientBuilder = webClientBuilder;
        authenticationServiceUrl = "http://" + authenticationServiceHost + ":" + authenticationServicePort + "/users";
    }


    public Mono<UserDetails> getUser(final int userId) {
        return webClientBuilder.build().get()
                .uri(authenticationServiceUrl + "/{userId}", userId)
=======

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
>>>>>>> 07aff82 (Implemented the delete user method, added testing.)
                .retrieve()
                .bodyToMono(UserDetails.class);
    }

<<<<<<< HEAD
    public Flux<UserDetails> getUsers() {
        return webClientBuilder.build().get()
                .uri(authenticationServiceUrl)
                .retrieve()
                .bodyToFlux(UserDetails.class);
    }

    public Mono<UserDetails> createUser (final UserDetails model){
        return webClientBuilder.build().post()
                .uri(authenticationServiceUrl + model)
=======
    public Mono<UserDetails> createUser (final UserDetails model) {
        return webClientBuilder.build().post()
                .uri(authServiceUrl + model)
>>>>>>> 07aff82 (Implemented the delete user method, added testing.)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(UserDetails.class);

    }

<<<<<<< HEAD
    public Flux<UserDetails> createUsers (){
        return webClientBuilder.build().post()
                .uri(authenticationServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToFlux(UserDetails.class);

    }

    public Mono<UserDetails> updateUser (final int userId, final UserDetails model){
        return webClientBuilder.build().put()
                .uri(authenticationServiceUrl + userId + model)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(UserDetails.class);
    }

}
=======
    public Mono<UserDetails> deleteUser(final long userId) {
        return webClientBuilder.build()
                .delete()
                .uri(authServiceUrl + "/{userId}", userId)
                .retrieve()
                .bodyToMono(UserDetails.class);
    }
}
>>>>>>> 07aff82 (Implemented the delete user method, added testing.)
