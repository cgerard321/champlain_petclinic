package com.petclinic.bffapigateway.utils.Security.Filters;


import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;

import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


@Slf4j
@Component
@Order(1)
public class JwtTokenFilter implements WebFilter {

    AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final AuthServiceClient authValidationService;

    private final JwtTokenUtil jwtTokenUtil;

    private final HashMap<String, String> AUTH_WHITELIST = new HashMap<>();
    //fill up the hashmap with the endpoints that are whitelisted


    public JwtTokenFilter(RequestMappingHandlerMapping requestMappingHandlerMapping, AuthServiceClient authValidationService, JwtTokenUtil jwtTokenUtil) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.authValidationService = authValidationService;
        this.jwtTokenUtil = jwtTokenUtil;

        //All white listed endpoints
        AUTH_WHITELIST.put("/v2/api-docs","/v2/api-docs");
        AUTH_WHITELIST.put("/swagger-resources","/swagger-resources");
        AUTH_WHITELIST.put("/swagger-resources/**","/swagger-resources/**");
        AUTH_WHITELIST.put("/configuration/ui","/configuration/ui");
        AUTH_WHITELIST.put("/configuration/security","/configuration/security");
        AUTH_WHITELIST.put("/swagger-ui.html","/swagger-ui.html");
        AUTH_WHITELIST.put("/webjars/**","/webjars/**");
        AUTH_WHITELIST.put("/v3/api-docs/**","/v3/api-docs/**");
        AUTH_WHITELIST.put("/swagger-ui/**","/swagger-ui/**");
        AUTH_WHITELIST.put("/scripts/**","/scripts/**");
        AUTH_WHITELIST.put("/css/**","/css/**");
        AUTH_WHITELIST.put("/images/**","/images/**");
        AUTH_WHITELIST.put("/images/*","/images/*");

    }



    @SuppressWarnings("NullableProblems")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        //todo optimize this
       if (AUTH_WHITELIST.keySet().stream().anyMatch(pattern -> antPathMatcher.match(pattern, path))) {
            log.debug("Request is a whitelisted endpoint, skipping filters !");
            exchange.getAttributes().put("whitelisted", true);

            return chain.filter(exchange);
        }

        if (Objects.requireNonNull(exchange.getRequest().getHeaders().get(HttpHeaders.ACCEPT)).get(0).contains("html")
                && path.equals("/")
                && exchange.getRequest().getMethod().toString().equals("GET")) {
            log.debug("Request is a browser request, skipping filters !");
            exchange.getAttributes().put("whitelisted", true);

            return chain.filter(exchange);
        }


        HandlerMethod handler = null;
        try {
            handler = (HandlerMethod) requestMappingHandlerMapping.getHandler(exchange).toFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


        //todo : remove this and replace with null check only for prod
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
