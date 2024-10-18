package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/roles")
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class RoleController {

    private final AuthServiceClient authServiceClient;

    @GetMapping
    public Flux<String> getAllRoles(@CookieValue("Bearer") String jwtToken) {
        return authServiceClient.getAllRoles(jwtToken);
    }

    @PostMapping
    public Mono<ResponseEntity<Role>> createRole(@CookieValue("Bearer") String jwtToken, @RequestBody String roleName) {
        return authServiceClient.createRole(jwtToken, roleName)
                .map(role -> ResponseEntity.status(HttpStatus.CREATED).body(role))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
}
