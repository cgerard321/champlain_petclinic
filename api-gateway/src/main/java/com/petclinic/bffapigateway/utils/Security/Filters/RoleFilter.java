package com.petclinic.bffapigateway.utils.Security.Filters;


import com.petclinic.bffapigateway.exceptions.InvalidTokenException;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@Order(2)
public class RoleFilter implements WebFilter {

    private final JwtTokenUtil jwtTokenUtil;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;



    public RoleFilter(JwtTokenUtil jwtTokenUtil, RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;

    }




    @SuppressWarnings("NullableProblems")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        HandlerMethod handler = null;
        try {
            handler = (HandlerMethod) requestMappingHandlerMapping.getHandler(exchange).toFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (exchange.getAttribute("whitelisted") != null && exchange.getAttribute("whitelisted") instanceof Boolean) {
            if((boolean) exchange.getAttribute("whitelisted")) {
                return chain.filter(exchange);
            }
        }

        if (handler.getMethod().getAnnotation(SecuredEndpoint.class) == null
                || handler.getMethod().getAnnotation(SecuredEndpoint.class).allowedRoles()[0] == Roles.ALL) {
            return chain.filter(exchange);
        }




        List<Roles> rolesAllowed = List.of(handler.getMethod().getAnnotation(SecuredEndpoint.class).allowedRoles());

        log.debug("Roles allowed: {}", rolesAllowed);

        if (rolesAllowed.isEmpty()) {
            return chain.filter(exchange);
        }
        String token = jwtTokenUtil.getTokenFromRequest(exchange);

        List<String> roles = jwtTokenUtil.getRolesFromToken(token);

        log.debug("Roles: {}", roles);

        if (roles == null) {
            return Mono.error(new InvalidTokenException("Unauthorized, invalid token"));
        }

        for (String role : roles) {
            role = role.replace("[", "")
                    .replace("]", "")
                    .replace(",","")
                    .trim();
            log.debug("Role: {}", role);

            if (rolesAllowed.contains(Roles.valueOf(role.toUpperCase()))) {
                log.debug("Role {} is allowed", role);
                return chain.filter(exchange);
            }
        }

        return Mono.error(new InvalidTokenException("Unauthorized, invalid token"));
    }
}
