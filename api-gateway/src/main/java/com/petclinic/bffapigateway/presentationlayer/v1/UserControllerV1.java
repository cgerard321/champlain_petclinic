package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.UserDetails;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

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
}