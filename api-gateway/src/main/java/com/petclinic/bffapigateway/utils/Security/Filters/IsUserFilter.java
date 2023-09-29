package com.petclinic.bffapigateway.utils.Security.Filters;


import com.petclinic.bffapigateway.exceptions.HandlerIsNullException;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.exceptions.InvalidTokenException;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
@Generated
public class IsUserFilter implements WebFilter {

    private final JwtTokenUtil jwtTokenUtil;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        log.debug("IsUserFilter");
        if (exchange.getAttribute("whitelisted") != null && exchange.getAttribute("whitelisted") instanceof Boolean) {
            if((boolean) exchange.getAttribute("whitelisted")) {
                return chain.filter(exchange);
            }
        }

        HandlerMethod handler;
        try {
            handler = (HandlerMethod) requestMappingHandlerMapping.getHandler(exchange).toFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new HandlerIsNullException(e.getMessage());
        }

        if (handler == null) {
            throw new HandlerIsNullException("Handler is null, check if the endpoint is valid");
        }

        if (handler.getMethod().getAnnotation(IsUserSpecific.class) == null) {
            return chain.filter(exchange);
        }


        if (Arrays.stream(handler.getMethod().getAnnotation(IsUserSpecific.class).bypassRoles()).anyMatch(role -> role == Roles.ALL)
        || Arrays.stream(handler.getMethod().getAnnotation(IsUserSpecific.class).bypassRoles()).anyMatch(role -> role == Roles.ANONYMOUS)) {

            if(Arrays.stream(handler.getMethod().getAnnotation(IsUserSpecific.class).bypassRoles()).anyMatch(role -> role == Roles.ANONYMOUS))
                log.warn("Endpoint is not secured, anyone can access it and annotation is redundant");


            return chain.filter(exchange);
        }
        else {

            String token = jwtTokenUtil.getTokenFromRequest(exchange);

            List<String> roles = jwtTokenUtil.getRolesFromToken(token);


            log.debug("Roles: {}", roles);


            if (roles == null) {
                return Mono.error(new InvalidTokenException("Unauthorized, invalid token"));
            }

            //todo : ask other teams if they want admin to have carte blanche
            //        if (roles.contains(Roles.ADMIN.toString())) {
            //            return chain.filter(exchange);
            //        }



            for (String role : roles) {
                role = role.replace("[", "")
                        .replace("]", "")
                        .replace(",","")
                        .trim();
                log.debug("Role: {}", role);

               if(Arrays.toString(handler.getMethod().getAnnotation(IsUserSpecific.class).bypassRoles()).contains(role)){
                   return chain.filter(exchange);
               }
            }






            String[] idToMatch = handler.getMethod().getAnnotation(IsUserSpecific.class).idToMatch();



            Map<String,String> pathVariables = exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);



            if (pathVariables == null){
                throw new InvalidInputException("You are not allowed to access this resource");
            }
            log.debug("Path variables: {}", pathVariables);



            log.debug("Token: {}", token);

            if (token == null) {
                log.debug("Token is null");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("No token provided".getBytes())));
            }

            String tokenId = jwtTokenUtil.getIdFromToken(token);

            log.debug("Token id: {}", tokenId);

            for (String id : idToMatch) {
                if (pathVariables.get(id) == null) {
                    throw new InvalidInputException("This is likely error is caused by assigning the wrong id to the annotation");
                }
                if (!pathVariables.get(id).equals(tokenId)) {
                    throw new InvalidInputException("You are not allowed to access this resource");
                }
            }

            return chain.filter(exchange);


        }

    }
}
