package com.petclinic.authservice.security;

import com.petclinic.authservice.datalayer.user.User;
import com.petclinic.authservice.datalayer.user.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Optional;


@Component
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;


    private final SecurityConst securityConst;
    private final JwtTokenUtil jwtTokenUtil;

    private final UserRepo userRepo;
    private final ObjectMapper objectMapper;


    public JwtTokenFilter(SecurityConst securityConst, JwtTokenUtil jwtTokenUtil
            , UserRepo userRepo, ObjectMapper objectMapper) {
        this.securityConst = securityConst;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepo = userRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @NotNull FilterChain chain)
            throws ServletException, IOException {


        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        log.warn("Entered Filter");
        // Get authorization header and validate
        final Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            log.info("No cookies found");
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
            log.info("No token found");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resolver.resolveException(request, response, null, new InvalidBearerTokenException("No Token Found"));
            return;
        }

        final String token = sessionCookie.getValue();
        log.info("Token: {}", token);

        try {

            if (!jwtTokenUtil.validateToken(token)) {
                log.info("Token is invalid");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resolver.resolveException(request, response, null, new InvalidBearerTokenException("Token is expired"));
                return;
            }

            log.info("Token is valid");
            Optional<User> userResponseModel = userRepo
                    .findByEmail(jwtTokenUtil.getUsernameFromToken(token));

            UserDetails userDetails = new UserPrincipalImpl(userResponseModel);


            UsernamePasswordAuthenticationToken
                    authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null,
                    userDetails.getAuthorities()
            );
            log.info("User details: {}",userDetails);
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            log.info("User authenticated");

            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } catch (Exception ex) {
            resolver.resolveException(request, response, null, ex);

        }
    }

}
