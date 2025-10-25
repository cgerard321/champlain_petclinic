package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway")
@Validated
public class UserController {

    private final AuthServiceClient authServiceClient;
    @Value("${frontend.url}")
    String frontendUrl;
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("/verification/{token}")
    public Mono<ResponseEntity<UserDetails>> verifyUserUsingV2Endpoint(@PathVariable final String token) {
        return authServiceClient.verifyUserUsingV2Endpoint(token)
                .map(userDetailsResponseEntity -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Location", frontendUrl+"/users/login");
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .headers(headers)
                            .body(userDetailsResponseEntity.getBody());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "/users",
            consumes = "application/json",
            produces = "application/json")
    public Mono<ResponseEntity<OwnerResponseDTO>> createUserUsingV2Endpoint(@RequestBody @Valid Mono<Register> model) {
        return authServiceClient.createUser(model).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping("/users")
    public Flux<UserDetails> getAllUsers(@CookieValue("Bearer") String jwtToken) {
        return authServiceClient.getAllUsers(jwtToken);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping("/users/{userId}")
    public Mono<ResponseEntity<UserDetails>> getUserById(@PathVariable final String userId, @CookieValue("Bearer") String jwtToken) {
        return authServiceClient.getUserById(jwtToken, userId)
                .map(userDetails -> ResponseEntity.ok().body(userDetails))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
                .onErrorResume(e -> {
                    log.error("Error fetching user by ID: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping("/users/{userId}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable final String userId, @CookieValue("Bearer") String jwtToken) {
        return authServiceClient.deleteUser(jwtToken, userId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Error deleting user: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Void>build());
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PatchMapping("/users/{userId}/disable")
    public Mono<ResponseEntity<Void>> disableUser(@PathVariable final String userId, @CookieValue("Bearer") String jwtToken) {
        return authServiceClient.disableUser(userId, jwtToken)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Error disabling user: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Void>build());
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PatchMapping("/users/{userId}/enable")
    public Mono<ResponseEntity<Void>> enableUser(@PathVariable final String userId, @CookieValue("Bearer") String jwtToken) {
        return authServiceClient.enableUser(userId, jwtToken)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Error enabling user: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Void>build());
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PutMapping(value = "users/{userId}",
            consumes = "application/json",
            produces = "application/json")
    public Mono<UserPasswordLessDTO> updateUser(final @PathVariable String userId,
                                                @RequestBody UserPasswordLessDTO model,
                                                @CookieValue("Bearer") String jwtToken) {
        return authServiceClient.updateUser(userId, model, jwtToken);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "/users/forgot_password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> sendForgotPasswordEmail(@RequestBody Mono<UserEmailRequestDTO> userEmailRequestDTO) {
        return authServiceClient.sendForgottenEmail(userEmailRequestDTO)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "/users/reset_password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> processResetPassword(@RequestBody @Valid Mono<UserPasswordAndTokenRequestModel> resetRequest) {
        return authServiceClient.changePassword(resetRequest)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PatchMapping(value = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserResponseDTO>> updateUserRoles(@PathVariable final String userId,
                                                                 @RequestBody final RolesChangeRequestDTO rolesChangeRequestDTO,
                                                                 @CookieValue("Bearer") String jwtToken) {
        return authServiceClient.updateUsersRoles(userId, rolesChangeRequestDTO, jwtToken)
                .map(userResponseDTO -> ResponseEntity.ok().body(userResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping(value = "/users/{userId}/username", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @IsUserSpecific(idToMatch = {"userId"})
    public Mono<ResponseEntity<String>> updateUsername(@PathVariable final String userId,
                                                       @RequestBody final String username,
                                                       @CookieValue("Bearer") String jwtToken
    ) {
        return authServiceClient.updateUsername(userId,username,jwtToken)
                .map(usernameChangeRequestDTO -> ResponseEntity.ok().body(usernameChangeRequestDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


}
