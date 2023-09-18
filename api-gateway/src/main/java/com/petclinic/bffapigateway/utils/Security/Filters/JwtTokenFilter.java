package com.petclinic.bffapigateway.utils.Security.Filters;


import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;

import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class JwtTokenFilter implements WebFilter {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final AuthServiceClient authValidationService;

    private final JwtTokenUtil jwtTokenUtil;


    @SuppressWarnings("NullableProblems")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HandlerMethod handler = null;
        try {
            handler = (HandlerMethod) requestMappingHandlerMapping.getHandler(exchange).toFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


        if(handler != null)
        {
            log.debug("Handler: {}", handler.getMethod().getName());
        }
        else
        {
            log.debug("Handler is null");
            throw new RuntimeException("Handler is null");
        }
        exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", "*");

        exchange.getResponse().getHeaders().add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");

        if (handler.getMethod().getAnnotation(SecuredEndpoint.class) != null) {
            if(Arrays.asList(handler.getMethod().getAnnotation(SecuredEndpoint.class).allowedRoles()).contains(Roles.ANONYMOUS))
            {
                log.debug("Anonymous request skipping filters !");
                exchange.getAttributes().put("whitelisted", true);

                return chain.filter(exchange);

            }
        }


        String token = jwtTokenUtil.getTokenFromRequest(exchange);
        log.debug("Token: {}", token);

        if (token == null) {
            log.debug("Token is null");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("No token provided".getBytes())));
        }



        Mono<ResponseEntity<Flux<String>>> validationResponse = authValidationService.validateToken(token);


        return validationResponse.flatMap(responseEntity -> {
            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                // Token is valid, proceed with the request

               log.debug("Token is valid");
               return chain.filter(exchange);
                }
        else {
            exchange.getResponse().setStatusCode(responseEntity.getStatusCode());

            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(("Authentication refused !\n"+ responseEntity.getBody()).getBytes() )));

        }
        });


    }
}
