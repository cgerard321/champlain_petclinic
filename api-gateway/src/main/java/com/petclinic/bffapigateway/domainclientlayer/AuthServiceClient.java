package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.*;
import com.petclinic.bffapigateway.dtos.Login;
import com.petclinic.bffapigateway.dtos.Register;
import com.petclinic.bffapigateway.dtos.Role;
import com.petclinic.bffapigateway.dtos.UserDetails;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.utils.Rethrower;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static reactor.core.publisher.Mono.error;
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
                .uri(authServiceUrl + "/users/withoutPages")
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
    public Mono<UserDetails> updateUser (final long userId, final Register model) {
        return webClientBuilder.build().put()
                .uri(authServiceUrl + "/users/{userId}", userId)
                .body(just(model), Register.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        n -> rethrower.rethrow(n,
                                x -> new GenericHttpException(x.get("message").toString(), BAD_REQUEST))
                        )
                .bodyToMono(UserDetails.class);
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
                .uri(authServiceUrl + "/users/verification/{token}", token)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        n -> rethrower.rethrow(n,
                                x -> new GenericHttpException(x.get("message").toString(), BAD_REQUEST))
                )
                .bodyToMono(UserDetails.class);
    }

    // NOTE: At the time I am writing this method, there is no way to get the body of a response
    // with exchange() other than doing more scuffed shit with AtomicReferences and even.
    // then it's extremely messy because it returns yet another god damned Mono.
    // Please take the time to look up if reactive web has added a fix
    // to this when you see this in the future.
    public Mono<Tuple2<String, UserDetails>> login(final Login login) {
        AtomicReference<String> token = new AtomicReference<>();

        return webClientBuilder.build()
                .post()
                .uri(authServiceUrl + "/users/login")
                .accept(MediaType.APPLICATION_JSON)
                .body(just(login), Login.class)
                .exchange()
                .doOnNext(n -> {
                    if(n.statusCode().is4xxClientError()) {
                        n.releaseBody();
                        throw new GenericHttpException("Unauthorized", UNAUTHORIZED);
                    }
                })
                .doOnSuccess(n ->  {
                    final List<String> strings = n.headers().asHttpHeaders().get(HttpHeaders.AUTHORIZATION);
                    if(strings == null || strings.size() == 0)return;
                    token.set(strings.get(0));
                })
                .switchIfEmpty(error(new RuntimeException("")))
                .flatMap(n -> n.bodyToMono(UserDetails.class))
                .map(n -> Tuples.of(token.get(), n));
    }


    public Flux<Role> getRoles() {
        return webClientBuilder.build().get()
                .uri(authServiceUrl + "/admin/roles")
                .retrieve()
                .bodyToFlux(Role.class);
    }

    public Mono<Role> addRole(final Role model) {
        return webClientBuilder.build().post()
                .uri(authServiceUrl + "/admin/roles")
                .body(just(model), Role.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Role.class);
    }

    public Mono<Void> deleteRole(final int id) {
        return webClientBuilder.build()
                .delete()
                .uri(authServiceUrl + "/admin/role/{id}", id)
                .retrieve()
                .bodyToMono(Void.class);
    }
}

