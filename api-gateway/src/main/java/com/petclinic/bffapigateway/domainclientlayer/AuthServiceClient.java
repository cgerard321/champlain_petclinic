package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetRequestDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import com.petclinic.bffapigateway.exceptions.*;
import com.petclinic.bffapigateway.utils.Rethrower;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Component
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;

    private final CustomersServiceClient customersServiceClient;

    private final VetsServiceClient vetsServiceClient;

    private final String authServiceUrl;

    @Autowired
    private Rethrower rethrower;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            CustomersServiceClient customersServiceClient, VetsServiceClient vetsServiceClient, @Value("${app.auth-service.host}") String authServiceHost,
            @Value("${app.auth-service.port}") String authServicePort) {
        this.webClientBuilder = webClientBuilder;
        this.customersServiceClient = customersServiceClient;
        this.vetsServiceClient = vetsServiceClient;
        authServiceUrl = "http://" + authServiceHost + ":" + authServicePort;
    }


    public Mono<UserDetails> getUserById(String jwtToken, long userId) {
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
    //FUCK REACTIVE
    /*
    This shit is beyond cursed, but I do not care. This works, I only spent 6 HOURS OF MY LIFE.
     */
        public Mono<OwnerResponseDTO> createUser (Mono<Register> model) {

            String uuid = UUID.randomUUID().toString();

            return model.flatMap(register ->{
                    register.setUserId(uuid);
                    return webClientBuilder.build().post()
                            .uri(authServiceUrl + "/users")
                            .body(Mono.just(register), Register.class)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError,
                                    n -> rethrower.rethrow(n,
                                            x -> new GenericHttpException(x.get("message").toString(), BAD_REQUEST))
                            )
                            .bodyToMono(UserPasswordLessDTO.class)
                            .flatMap(userDetails -> {
                                OwnerRequestDTO ownerRequestDTO = OwnerRequestDTO.builder()
                                        .firstName(register.getOwner().getFirstName())
                                        .lastName(register.getOwner().getLastName())
                                        .address(register.getOwner().getAddress())
                                        .city(register.getOwner().getCity())
                                        .province(register.getOwner().getProvince())
                                        .telephone(register.getOwner().getTelephone())
                                        .ownerId(uuid)
                                        .build();
                                return customersServiceClient.createOwner(ownerRequestDTO);
                            }
                            );
            }
            ).doOnError(throwable -> {
                log.error("Error creating user: " + throwable.getMessage());
                customersServiceClient.deleteOwner(uuid);
            });

        }


    public Mono<VetResponseDTO> createVetUser(Mono<RegisterVet> model){

        String uuid = UUID.randomUUID().toString();
        log.info("UUID: " + uuid);

        return model.flatMap(registerVet -> {
            registerVet.setUserId(uuid);
            return webClientBuilder.build().post()
                    .uri(authServiceUrl + "/users")
                    .body(Mono.just(registerVet), RegisterVet.class)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            n -> rethrower.rethrow(n,
                                    x -> new GenericHttpException(x.get("message").toString(),(HttpStatus) n.statusCode()))
                    )
                    .bodyToMono(UserPasswordLessDTO.class)
                    .flatMap(userDetails -> {
                                VetRequestDTO vetDTO = VetRequestDTO.builder()
                                        .specialties(registerVet.getVet().getSpecialties())
                                        .active(registerVet.getVet().isActive())
                                        .email(registerVet.getEmail())
                                        .resume(registerVet.getVet().getResume())
                                        .workday(registerVet.getVet().getWorkday())
                                        .phoneNumber(registerVet.getVet().getPhoneNumber())
                                        .vetBillId(registerVet.getVet().getVetBillId())
                                        .firstName(registerVet.getVet().getFirstName())
                                        .lastName(registerVet.getVet().getLastName())
                                        .vetId(uuid)
                                        .build();
                                return vetsServiceClient.createVet((Mono.just(vetDTO)));
                            }
                    );
        }).doOnError(throwable -> {
            log.error("Error creating user: " + throwable.getMessage());
            vetsServiceClient.deleteVet(uuid);
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

    public Mono<ResponseEntity<UserDetails>> verifyUser(final String token) {

        return webClientBuilder.build()
                .get()
                .uri(authServiceUrl + "/users/verification/{token}", token)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        n -> rethrower.rethrow(
                                n,
                                x -> new GenericHttpException(x.get("message").toString(),(HttpStatus) n.statusCode()))
                )
                //grabbing the response entity and modifying the headers a little before returning it
                .toEntity(UserDetails.class)
                .map(responseEntity -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Location", "http://localhost:8080/#!/login");
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .headers(headers)
                            .body(responseEntity.getBody());
                });
    }

    public  Mono<ResponseEntity<UserPasswordLessDTO>> login(final Mono<Login> login) throws Exception {
        try {
            return webClientBuilder.build()
                    .post()
                    .uri(authServiceUrl+"/users/login")
                    .body(login, Login.class)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse.bodyToMono(HttpErrorInfo.class)
                            .flatMap(error -> Mono.error(new InvalidCredentialsException(error.getMessage()))))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new InvalidInputException("Invalid token")))
                    .toEntity(UserPasswordLessDTO.class);
        } catch (HttpClientErrorException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
    }


    public Mono<ResponseEntity<Void>> sendForgottenEmail(Mono<UserEmailRequestDTO> emailRequestDTOMono) {


        try {
            String url = authServiceUrl+"/users/forgot_password";

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .body(emailRequestDTOMono, UserEmailRequestDTO.class)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new InvalidInputException("Unexpected error")))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new InvalidInputException("Unexpected error")))
                    .toEntity(Void.class);

        } catch (HttpClientErrorException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
    }

    public Mono<ResponseEntity<Void>> changePassword(Mono<UserPasswordAndTokenRequestModel> pwdChange) {


        try {
            String url = authServiceUrl+"/users/reset_password";

            return webClientBuilder.build()
                            .post().uri(url)
                            .body(pwdChange, UserPasswordAndTokenRequestModel.class)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, n -> rethrower.rethrow(n,
                            x -> new GenericHttpException(x.get("message").toString(), (HttpStatus) n.statusCode())))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new InvalidInputException("Unexpected error")))
                    .toEntity(Void.class);



        } catch (HttpClientErrorException ex) {
            throw new InvalidInputException(ex.getMessage());
        }
    }


    public Mono<ResponseEntity<TokenResponseDTO>> validateToken(String jwtToken) {
        // Make a POST request to the auth-service for token validation

        return webClientBuilder.build()
                .post()
                .uri(authServiceUrl + "/users/validate-token")
                .cookie("Bearer", jwtToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new InvalidTokenException("Invalid token")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new InvalidInputException("Invalid token")))
                .toEntity(TokenResponseDTO.class);
    }

    public Mono<UserResponseDTO> updateUsersRoles(String userId, RolesChangeRequestDTO rolesChangeRequestDTO, String jwToken) {
        return webClientBuilder.build()
                .patch()
                .uri(authServiceUrl + "/users/{userId}", userId)
                .bodyValue(rolesChangeRequestDTO)
                .cookie("Bearer", jwToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, n -> rethrower.rethrow(n,
                        x -> new GenericHttpException(x.get("message").toString(), (HttpStatus) n.statusCode())))
                .bodyToMono(UserResponseDTO.class);
    }

}

