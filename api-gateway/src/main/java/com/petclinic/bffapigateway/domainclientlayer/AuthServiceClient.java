package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidCredentialsException;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.utils.Rethrower;
import com.petclinic.bffapigateway.exceptions.InvalidTokenException;
import com.petclinic.bffapigateway.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static reactor.core.publisher.Mono.just;

@Slf4j
@Component
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;

    private final CustomersServiceClient customersServiceClient;
    private final String authServiceUrl;

    @Autowired
    private Rethrower rethrower;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            CustomersServiceClient customersServiceClient, @Value("${app.auth-service.host}") String authServiceHost,
            @Value("${app.auth-service.port}") String authServicePort) {
        this.webClientBuilder = webClientBuilder;
        this.customersServiceClient = customersServiceClient;
        authServiceUrl = "http://" + authServiceHost + ":" + authServicePort;
    }


//    public Mono<UserDetails> getUser(final long userId) {
//        return webClientBuilder.build().get()
//                .uri(authServiceUrl + "/users/{userId}", userId)
//                .retrieve()
//                .bodyToMono(UserDetails.class);
//    }
//
    public Flux<UserDetails> getUsers(String jwtToken) {
        return webClientBuilder.build().get()
                .uri(authServiceUrl + "/users/withoutPages")
                .cookie("Bearer", jwtToken)
                .retrieve()
                .bodyToFlux(UserDetails.class);
    }
//
        public Mono<OwnerResponseDTO> createUser (Register model) {

            String uuid = UUID.randomUUID().toString();
            model.setUserId(uuid);
            model.getOwner().setOwnerId(uuid);
            log.info("Entered domain service create user");
            log.info("UUID : {}",uuid);

            return webClientBuilder.build().post()
                    .uri(authServiceUrl + "/users")
                    .body(just(model), Register.class)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            n -> rethrower.rethrow(n,
                                    x -> new GenericHttpException(x.get("message").toString(), BAD_REQUEST)))
                    .onStatus(HttpStatusCode::is5xxServerError,  n -> rethrower.rethrow(n,
                            x -> new GenericHttpException(x.get("message").toString(), BAD_REQUEST)))
                    .bodyToMono(UserPasswordLessDTO.class)
                    .then(customersServiceClient.createOwner(model.getOwner()))
                    .doOnError(e -> {
                         customersServiceClient.deleteOwner(uuid);
                    });
        }

//    public Mono<UserDetails> updateUser (final long userId, final Register model) {
//        return webClientBuilder.build().put()
//                .uri(authServiceUrl + "/users/{userId}", userId)
//                .body(just(model), Register.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
//                .onStatus(HttpStatus::is4xxClientError,
//                        n -> rethrower.rethrow(n,
//                                x -> new GenericHttpException(x.get("message").toString(), BAD_REQUEST))
//                        )
//                .bodyToMono(UserDetails.class);
//    }
//
//    public Mono<UserDetails> deleteUser(String auth, final long userId) {
//        return webClientBuilder.build()
//                .delete()
//                .uri(authServiceUrl + "/users/{userId}", userId)
//                .header("Authorization", auth)
//                .retrieve()
//                .bodyToMono(UserDetails.class);
//    }

    public Mono<UserDetails> verifyUser(final String token) {
        return webClientBuilder.build()
                .get()
                .uri(authServiceUrl + "/users/verification/{token}", token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        n -> rethrower.rethrow(n,
                                x -> new GenericHttpException(x.get("message").toString(), BAD_REQUEST))
                )
                .bodyToMono(UserDetails.class);
    }

    public  Mono<ResponseEntity<UserPasswordLessDTO>> login(final Login login) throws Exception {
        log.info("Entered domain service login");
        try {
            log.info("Email : {}",login.getEmail());
            return webClientBuilder.build()
                    .post()
                    .uri(authServiceUrl+"/users/login")
                    .bodyValue(login)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new InvalidCredentialsException("Invalid token")))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new InvalidInputException("Invalid token")))
                    .toEntity(UserPasswordLessDTO.class);

//            HttpEntity<Login> userRequestModelHttpEntity = new HttpEntity<>(login);
//
//            HttpEntity<UserPasswordLessDTO> response = restTemplate.exchange(authServiceUrl + "/users/login", HttpMethod.POST, userRequestModelHttpEntity, UserPasswordLessDTO.class);
//            log.info("Fetched user from auth-service");
//            return Mono.just(response);
        } catch (HttpClientErrorException ex) {
            log.info("Error throw in auth domain client service");
            throw new InvalidInputException(ex.getMessage());
        }
    }


    public Mono<ResponseEntity<Void>> sendForgottenEmail(ServerHttpRequest request, String email) {

        UserResetPwdRequestModel userResetPwdRequestModel = UserResetPwdRequestModel.builder().email(email).url(Utility.getSiteURL(request)).build();

        try {
            String url = authServiceUrl+"/users/forgot_password";

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .bodyValue(userResetPwdRequestModel)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new InvalidInputException("Unexpected error")))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new InvalidInputException("Unexpected error")))
                    .toEntity(Void.class);

        } catch (HttpClientErrorException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
    }

    public Mono<ResponseEntity<Void>> changePassword(UserPasswordAndTokenRequestModel pwdChange) {

        UserResetPwdWithTokenRequestModel userResetPwdWithTokenRequestModel = UserResetPwdWithTokenRequestModel.builder().token(pwdChange.getToken()).password(pwdChange.getPassword()).build();

        log.info("Token : {}",pwdChange.getToken());
        log.info("Password : {}",pwdChange.getPassword());
        try {
            String url = authServiceUrl+"/users/reset_password";

            return webClientBuilder.build()
                            .post().uri(url)
                            .bodyValue(userResetPwdWithTokenRequestModel)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new InvalidInputException("Unexpected error")))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new InvalidInputException("Unexpected error")))
                    .toEntity(Void.class);



        } catch (HttpClientErrorException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
    }


    public Mono<ResponseEntity<Flux<String>>> validateToken(String jwtToken) {
        // Make a POST request to the auth-service for token validation

        return webClientBuilder.build()
                .post()
                .uri(authServiceUrl + "/users/validate-token")
                .cookie("Bearer", jwtToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new InvalidTokenException("Invalid token")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new InvalidInputException("Invalid token")))
                .toEntityFlux(String.class);
    }

}

