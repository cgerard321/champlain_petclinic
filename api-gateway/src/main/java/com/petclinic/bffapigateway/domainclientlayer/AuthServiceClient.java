package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.utils.Rethrower;
import com.petclinic.bffapigateway.exceptions.InvalidTokenException;
import com.petclinic.bffapigateway.utils.Security.Variables.SecurityConst;
import com.petclinic.bffapigateway.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Slf4j
@Component
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String authServiceUrl;

    private final RestTemplate restTemplate;

    private final SecurityConst securityConst;

    @Autowired
    private Rethrower rethrower;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.auth-service.host}") String authServiceHost,
            @Value("${app.auth-service.port}") String authServicePort,
            RestTemplate restTemplate, SecurityConst securityConst) {
        this.webClientBuilder = webClientBuilder;
        this.restTemplate = restTemplate;
        this.securityConst = securityConst;
        authServiceUrl = "http://" + authServiceHost + ":" + authServicePort;
    }


//    public Mono<UserDetails> getUser(final long userId) {
//        return webClientBuilder.build().get()
//                .uri(authServiceUrl + "/users/{userId}", userId)
//                .retrieve()
//                .bodyToMono(UserDetails.class);
//    }
//
//    public Flux<UserDetails> getUsers(String auth) {
//        return webClientBuilder.build().get()
//                .uri(authServiceUrl + "/users/withoutPages")
//                .header("Authorization", auth)
//                .retrieve()
//                .bodyToFlux(UserDetails.class);
//    }
//
//    public Mono<UserDetails> createUser (final Register model) {
//        return webClientBuilder.build().post()
//                .uri(authServiceUrl + "/users")
//                .body(just(model), Register.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
//                .onStatus(HttpStatus::is4xxClientError,
//                        n -> rethrower.rethrow(n,
//                                x -> new GenericHttpException(x.get("message").toString(), BAD_REQUEST))
//                        )
//                .bodyToMono(UserDetails.class);
//    }
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
//
//    public Mono<UserDetails> verifyUser(final String token) {
//        return webClientBuilder.build()
//                .get()
//                .uri(authServiceUrl + "/users/verification/{token}", token)
//                .retrieve()
//                .onStatus(HttpStatus::is4xxClientError,
//                        n -> rethrower.rethrow(n,
//                                x -> new GenericHttpException(x.get("message").toString(), BAD_REQUEST))
//                )
//                .bodyToMono(UserDetails.class);
//    }

    public  HttpEntity<UserPasswordLessDTO> login(final Login login) throws Exception {
        log.info("Entered domain service login");
        UserPasswordLessDTO userResponseModel;
        try {
            log.info("Email : {}",login.getEmail());
            HttpEntity<Login> userRequestModelHttpEntity = new HttpEntity<>(login);

            HttpEntity<UserPasswordLessDTO> response = restTemplate.exchange(authServiceUrl + "/users/login", HttpMethod.POST, userRequestModelHttpEntity, UserPasswordLessDTO.class);
            log.info("Fetched user from auth-service");
            return response;

        } catch (HttpClientErrorException ex) {
            log.info("Error throw in auth domain client service");
            throw new Exception(ex);
        }
    }

    public String userForgotPassword() {
        String form;
        try {
            String url = authServiceUrl + "/users/forgot_password";
            form = restTemplate
                    .getForObject(url, String.class);

        } catch (HttpClientErrorException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
        return form;
    }

    public String sendForgottenEmail(ServerHttpRequest request, String email) {

        UserResetPwdRequestModel userResetPwdRequestModel = UserResetPwdRequestModel.builder().email(email).url(Utility.getSiteURL(request)).build();

        String formPage;
        try {
            String url = authServiceUrl+"/users/forgot_password";
            formPage = restTemplate
                    .postForObject(url, userResetPwdRequestModel, String.class);

        } catch (HttpClientErrorException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
        return formPage;
    }
    public String userShowResetPage(String token) {
        String form;
        try {
            String url = authServiceUrl + "/users/reset_password";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            builder.queryParam("token",token);
            url = builder.toUriString();

            form = restTemplate
                    .getForObject(url, String.class);

        } catch (HttpClientErrorException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
        return form;
    }
    public String changePassword(ServerHttpRequest request,UserPasswordAndTokenRequestModel pwdChange) {

        UserResetPwdWithTokenRequestModel userResetPwdWithTokenRequestModel = UserResetPwdWithTokenRequestModel.builder().token(pwdChange.getToken()).password(pwdChange.getPassword()).build();

        log.info("Token : {}",pwdChange.getToken());
        log.info("Password : {}",pwdChange.getPassword());
        String formPage;
        try {
            String url = authServiceUrl+"/users/reset_password";
            formPage = restTemplate
                    .postForObject(url, userResetPwdWithTokenRequestModel, String.class);

        } catch (HttpClientErrorException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
        return formPage;
    }


    public Mono<ResponseEntity<Flux<String>>> validateToken(String jwtToken) {
        // Make a POST request to the auth-service for token validation

        return webClientBuilder.build()
                .post()
                .uri(authServiceUrl + "/users/validate-token")
                .bodyValue(jwtToken)
                .cookie("Bearer", jwtToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new InvalidTokenException("Invalid token")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new InvalidInputException("Invalid token")))
                .toEntityFlux(String.class);
    }

}

