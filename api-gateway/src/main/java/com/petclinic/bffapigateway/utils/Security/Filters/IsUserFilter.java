package com.petclinic.bffapigateway.utils.Security.Filters;


import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
import com.petclinic.bffapigateway.exceptions.ForbiddenAccessException;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.exceptions.InvalidTokenException;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import com.petclinic.bffapigateway.utils.Utility;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
@Generated
public class IsUserFilter implements WebFilter {

    private final Utility utility;

    // Probabbly a better way
    @SuppressWarnings("NullableProblems")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        if (exchange.getAttribute("whitelisted") != null && exchange.getAttribute("whitelisted") instanceof Boolean) {
            if((boolean) exchange.getAttribute("whitelisted")) {
                return chain.filter(exchange);
            }
        }

        HandlerMethod handler = utility.getHandler(exchange);

        if (handler.getMethod().getAnnotation(IsUserSpecific.class) == null) {
            return chain.filter(exchange);
        }


        if (Arrays.stream(handler.getMethod().getAnnotation(IsUserSpecific.class).bypassRoles()).anyMatch(role -> role == Roles.ALL)
        || Arrays.stream(handler.getMethod().getAnnotation(IsUserSpecific.class).bypassRoles()).anyMatch(role -> role == Roles.ANONYMOUS)) {

            if(Arrays.stream(handler.getMethod().getAnnotation(IsUserSpecific.class).bypassRoles()).anyMatch(role -> role == Roles.ANONYMOUS))
                log.error("Endpoint is not secured, anyone can access it and annotation is redundant. This is likely caused because the bypassRoles array contains ANONYMOUS.");


        }
        else {


            TokenResponseDTO tokenResponseDTO = exchange.getAttribute("tokenValues");


            if (tokenResponseDTO == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("No token provided".getBytes())));
            }

            List<String> roles = tokenResponseDTO.getRoles();




            if (roles == null) {
                return Mono.error(new InvalidTokenException("Unauthorized, invalid token"));
            }


            for (String role : roles) {
                role = role.replace("[", "")
                        .replace("]", "")
                        .replace(",","")
                        .trim();

               if(Arrays.toString(handler.getMethod().getAnnotation(IsUserSpecific.class).bypassRoles()).contains(role)){
                   return chain.filter(exchange);
               }
            }

            String[] idToMatch = handler.getMethod().getAnnotation(IsUserSpecific.class).idToMatch();



            Map<String,String> pathVariables = exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);



            if (pathVariables == null){
                throw new ForbiddenAccessException("You are not allowed to access this resource");
            }

            String tokenId = tokenResponseDTO.getUserId();


            for (String id : idToMatch) {
                if (pathVariables.get(id) == null) {
                    throw new InvalidInputException("This is likely error is caused by assigning the wrong id to the annotation");
                }
                if (!pathVariables.get(id).equals(tokenId)) {
                    throw new ForbiddenAccessException("You are not allowed to access this resource");
                }
            }


        }
        return chain.filter(exchange);

    }
}
