package com.petclinic.authservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Component
@Slf4j
@Generated
public class JwtTokenFilter extends OncePerRequestFilter {
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    private final SecurityConst securityConst;
    private final JwtTokenUtil jwtTokenUtil;
    private final List<AntPathRequestMatcher> excludedPaths = Arrays.asList(
            new AntPathRequestMatcher("/actuator/prometheus", "GET"),
            new AntPathRequestMatcher("/users/*/public-profile", "GET"));

    public JwtTokenFilter(SecurityConst securityConst, JwtTokenUtil jwtTokenUtil) {
        this.securityConst = securityConst;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    // Skip processing for public paths
    @Override
    protected boolean shouldNotFilter(@NotNull HttpServletRequest request) {
        return excludedPaths.stream().anyMatch(p -> p.matches(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain chain)
            throws ServletException, IOException {

        final Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            log.debug("No cookies found");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            chain.doFilter(request, response);
            return;
        }

        Cookie sessionCookie = null;
        for (Cookie cookie : cookies) {
            if ((securityConst.getTOKEN_PREFIX()).equals(cookie.getName())) {
                sessionCookie = cookie;
                break;
            }
        }

        if (sessionCookie == null) {
            log.debug("No token found");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resolver.resolveException(request, response, null, new InvalidBearerTokenException("Unauthorized"));
            return;
        }

        final String token = sessionCookie.getValue();

        try {
            if (!jwtTokenUtil.validateToken(token)) {
                log.debug("Token is invalid or expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resolver.resolveException(request, response, null, new InvalidBearerTokenException("Token is expired"));
                return;
            }

            String email = jwtTokenUtil.getUsernameFromToken(token);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, List.of());
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } catch (Exception ex) {
            resolver.resolveException(request, response, null, ex);
        }
    }
}