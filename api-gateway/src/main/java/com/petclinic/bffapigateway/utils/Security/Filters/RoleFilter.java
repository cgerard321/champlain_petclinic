package com.petclinic.bffapigateway.utils.Security.Filters;


import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
import com.petclinic.bffapigateway.exceptions.ForbiddenAccessException;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import com.petclinic.bffapigateway.utils.Utility;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Order(2)
@Generated
public class RoleFilter implements WebFilter {

   private final Utility utility;

    public RoleFilter(Utility utility) {
        this.utility = utility;
    }


    @SuppressWarnings("NullableProblems")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {


        if (exchange.getAttribute("whitelisted") != null && exchange.getAttribute("whitelisted") instanceof Boolean) {
            if ((boolean) exchange.getAttribute("whitelisted")) {
                return chain.filter(exchange);
            }
        }


        HandlerMethod handler = utility.getHandler(exchange);



        if (handler.getMethod().getAnnotation(SecuredEndpoint.class) == null || Arrays.stream(handler.getMethod().getAnnotation(SecuredEndpoint.class).allowedRoles()).anyMatch(role -> role == Roles.ALL)) {
            return chain.filter(exchange);
        }


        List<Roles> rolesAllowed = List.of(handler.getMethod().getAnnotation(SecuredEndpoint.class).allowedRoles());

        log.debug("Roles allowed: {}", rolesAllowed);

        if (rolesAllowed.isEmpty()) {
            return chain.filter(exchange);
        }
        TokenResponseDTO tokenResponseDTO = exchange.getAttribute("tokenValues");

        if (tokenResponseDTO == null) {
            return Mono.error(new ForbiddenAccessException("No token attached to request"));
        }

        List<String> roles = tokenResponseDTO.getRoles();


        log.debug("Roles: {}", roles);


        if (roles == null) {
            return Mono.error(new ForbiddenAccessException("No roles attached to token"));
        }


        for (String role : roles) {
            role = role.replace("[", "").replace("]", "").replace(",", "").trim();
            log.debug("Role: {}", role);

            if (rolesAllowed.contains(Roles.valueOf(role.toUpperCase()))) {
                log.debug("Role {} is allowed", role);
                return chain.filter(exchange);
            }
        }

        return Mono.error(new ForbiddenAccessException("Unauthorized, you do not possess the necessary permissions to access the endpoint"));
    }
}
