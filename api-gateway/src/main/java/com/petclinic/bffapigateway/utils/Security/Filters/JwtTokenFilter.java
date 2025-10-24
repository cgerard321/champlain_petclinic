package com.petclinic.bffapigateway.utils.Security.Filters;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import com.petclinic.bffapigateway.utils.Utility;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

@Slf4j
@Component
@Order(1)
@Generated
public class JwtTokenFilter implements WebFilter {

    AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final AuthServiceClient authValidationService;

    private final JwtTokenUtil jwtTokenUtil;

    private final Utility utility;

    private final HashMap<String, String> AUTH_WHITELIST = new HashMap<>();
    // fill up the hashmap with the endpoints that are whitelisted

    @Value("${frontend.url}")
    private String frontendOrigin;


    public JwtTokenFilter(AuthServiceClient authValidationService, JwtTokenUtil jwtTokenUtil, Utility utility) {
        this.authValidationService = authValidationService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.utility = utility;
        // All white listed endpoints
        AUTH_WHITELIST.put("/context-path/swagger-ui", "/context-path/swagger-ui");
        AUTH_WHITELIST.put("/custom/swagger-ui.html", "/custom/swagger-ui.html");
        AUTH_WHITELIST.put("/swagger-resources", "/swagger-resources");
        AUTH_WHITELIST.put("/swagger-resources/**", "/swagger-resources/**");
        AUTH_WHITELIST.put("/configuration/ui", "/configuration/ui");
        AUTH_WHITELIST.put("/configuration/security", "/configuration/security");
        AUTH_WHITELIST.put("/swagger-ui.html", "/swagger-ui.html");
        AUTH_WHITELIST.put("/webjars/**", "/webjars/**");
        AUTH_WHITELIST.put("/api-docs", "/api-docs");
        AUTH_WHITELIST.put("/api-docs/**", "/api-docs/**");

        AUTH_WHITELIST.put("/swagger-ui/**", "/swagger-ui/**");
        AUTH_WHITELIST.put("/scripts/**", "/scripts/**");
        AUTH_WHITELIST.put("/css/**", "/css/**");
        AUTH_WHITELIST.put("/images/**", "/images/**");
        AUTH_WHITELIST.put("/images/*", "/images/*");

        AUTH_WHITELIST.put("/actuator/health", "/actuator/health");
        AUTH_WHITELIST.put("/health", "/health");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", frontendOrigin);

        exchange.getResponse().getHeaders().add("Access-Control-Allow-Credentials", "true");

        exchange.getResponse().getHeaders().add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS, PATCH, HEAD");

        exchange.getResponse().getHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        // todo optimize this
        if (exchange.getRequest().getMethod().equals(HttpMethod.OPTIONS)
                || AUTH_WHITELIST.keySet().stream().anyMatch(pattern -> antPathMatcher.match(pattern, path))) {
            exchange.getAttributes().put("whitelisted", true);

            return chain.filter(exchange);
        }

        if (Objects.requireNonNull(exchange.getRequest().getHeaders().get(HttpHeaders.ACCEPT)).get(0).contains("html")
                && path.equals("/") && exchange.getRequest().getMethod().toString().equals("GET")) {
            exchange.getAttributes().put("whitelisted", true);

            return chain.filter(exchange);
        }

        HandlerMethod handler = utility.getHandler(exchange);

        if (handler.getMethod().getAnnotation(SecuredEndpoint.class) != null) {
            if (Arrays.asList(handler.getMethod().getAnnotation(SecuredEndpoint.class).allowedRoles())
                    .contains(Roles.ANONYMOUS)) {
                exchange.getAttributes().put("whitelisted", true);

                return chain.filter(exchange);
            }
        }

        String token = jwtTokenUtil.getTokenFromRequest(exchange);

        if (token == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("No token provided".getBytes())));
        }

        Mono<ResponseEntity<TokenResponseDTO>> validationResponse = authValidationService.validateToken(token);

        return validationResponse.flatMap(responseEntity -> {
            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                // Token is valid, proceed with the request
                exchange.getAttributes().put("tokenValues", responseEntity.getBody());

                return chain.filter(exchange);
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

                return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
                        .wrap(("Authentication refused !\n" + responseEntity.getBody()).getBytes())));
            }
        });

    }
}
