package com.petclinic.bffapigateway.utils.Security.Filters;

import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import com.petclinic.bffapigateway.utils.Utility;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
@Generated
public class CsrfFilter implements WebFilter {

    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    private final Utility utility;

    private static final Set<HttpMethod> SAFE_METHODS = Set.of(
            HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.TRACE);

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();
        String path = exchange.getRequest().getURI().getPath();
        log.info("URI Path seen by CsrfFilter: {}", exchange.getRequest().getURI().getPath());
        HandlerMethod handler = utility.getHandler(exchange);
        boolean isExluded = false;

        if (handler.getMethod().getAnnotation(SecuredEndpoint.class) != null) {
            if (Arrays.asList(handler.getMethod().getAnnotation(SecuredEndpoint.class).allowedRoles())
                    .contains(Roles.ANONYMOUS)) {
                isExluded = true;
            }
        }

        if (SAFE_METHODS.contains(method) || isExluded) {
            return chain.filter(exchange);
        }

        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(CSRF_COOKIE_NAME);
        String cookieToken = cookie != null ? cookie.getValue() : null;
        String headerToken = exchange.getRequest().getHeaders().getFirst(CSRF_HEADER_NAME);

        if (cookieToken == null || !cookieToken.equals(headerToken)) {
            log.warn("CSRF check failed for {} {}", method, path);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().writeWith(Mono.just(
                    exchange.getResponse().bufferFactory().wrap(
                            "CSRF token missing or invalid".getBytes())));
        }

        return chain.filter(exchange);
    }
}