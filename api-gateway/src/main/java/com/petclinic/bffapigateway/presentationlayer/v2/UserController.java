package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.Register;
import com.petclinic.bffapigateway.dtos.Auth.UserDetails;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80, http://localhost:8080")
public class UserController {

    private final AuthServiceClient authServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("/verification/{token}")
    public Mono<ResponseEntity<UserDetails>> verifyUserUsingV2Endpoint(@PathVariable final String token) {
        return authServiceClient.verifyUserUsingV2Endpoint(token)
                .map(userDetailsResponseEntity -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Location", "http://localhost:3000/users/login");
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
        return authServiceClient.createUserUsingV2Endpoint(model).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
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
}
