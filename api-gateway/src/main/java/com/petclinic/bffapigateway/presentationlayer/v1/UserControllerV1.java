package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("api/gateway/users")
public class UserControllerV1 {

    private final AuthServiceClient authServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.OWNER, Roles.ADMIN})
    @GetMapping("/{userId}")
    public Mono<ResponseEntity<UserDetails>> getUserById(@PathVariable String userId, @CookieValue("Bearer") String token) {
        return authServiceClient.getUserById(token, userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"userId"}, bypassRoles = {Roles.ADMIN})
    @PatchMapping("/{userId}/username")
    public Mono<ResponseEntity<String>> updateUsername(
            @PathVariable String userId,
            @RequestBody @NotBlank @Size(min = 3, max = 30) @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9_]+$", message = "Username must contain at least one letter and can only contain letters, numbers, and underscores") String username,
            @CookieValue("Bearer") String token) {
        return authServiceClient.updateUsername(userId, username, token)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER, Roles.ADMIN})
    @GetMapping("/username/check")
    public Mono<ResponseEntity<Boolean>> checkUsernameAvailability(
            @RequestParam String username,
            @CookieValue("Bearer") String token) {
        return authServiceClient.checkUsernameAvailability(username, token)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.ok(false));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("/verification/{token}")
    public Mono<ResponseEntity<UserDetails>> verifyUser(@PathVariable final String token) {
        return authServiceClient.verifyUser(token)
                .map(userDetailsResponseEntity -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Location", "http://localhost:8080/#!/login");
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .headers(headers)
                            .body(userDetailsResponseEntity.getBody());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<OwnerResponseDTO>> createUser(@RequestBody @Valid Mono<Register> model) {
        return authServiceClient.createUser(model).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserDetails> getAllUsers(@CookieValue("Bearer") String auth, @RequestParam Optional<String> username) {
        if(username.isPresent()) {
            return authServiceClient.getUsersByUsername(auth, username.get());
        }
        else {
            return authServiceClient.getUsers(auth);
        }
    }

    @PatchMapping(value = "/{userId}", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<UserResponseDTO>> updateUserRoles(final @PathVariable String userId, @RequestBody RolesChangeRequestDTO roleChangeDTO, @CookieValue("Bearer") String auth) {
        return authServiceClient.updateUsersRoles(userId, roleChangeDTO, auth)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> deleteUserById(@PathVariable String userId, @CookieValue("Bearer") String auth) {
        return authServiceClient.deleteUser(auth, userId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation()
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "/login",produces = "application/json;charset=utf-8;", consumes = "application/json")
    public Mono<ResponseEntity<UserPasswordLessDTO>> login(@RequestBody Mono<Login> login) throws Exception {
        log.info("Entered controller /login");
        return authServiceClient.login(login);

    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerHttpRequest request, ServerHttpResponse response) {
        return authServiceClient.logout(request, response);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping(value = "/forgot_password")
    public Mono<ResponseEntity<Void>> processForgotPassword(@RequestBody Mono<UserEmailRequestDTO> email) {
        return authServiceClient.sendForgottenEmail(email);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PostMapping("/reset_password")
    public Mono<ResponseEntity<Void>> processResetPassword(@RequestBody @Valid Mono<UserPasswordAndTokenRequestModel> resetRequest) {
        return authServiceClient.changePassword(resetRequest);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/inventoryManager")
    public Mono<ResponseEntity<UserPasswordLessDTO>> createInventoryManager(@RequestBody @Valid Mono<RegisterInventoryManager> model) {
        return authServiceClient.createInventoryMangerUser(model).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/vets",consumes = "application/json",produces = "application/json")
    public Mono<ResponseEntity<VetResponseDTO>> insertVet(@RequestBody Mono<RegisterVet> vetDTOMono) {
        return authServiceClient.createVetUser(vetDTOMono)
                .map(v->ResponseEntity.status(HttpStatus.CREATED).body(v))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
}