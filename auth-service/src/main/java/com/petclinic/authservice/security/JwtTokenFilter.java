package com.petclinic.authservice.security;

import com.petclinic.authservice.datalayer.user.User;
import com.petclinic.authservice.datalayer.user.UserRepo;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import java.util.List;
import java.util.Arrays;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import java.io.IOException;
import java.util.Optional;


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
            new AntPathRequestMatcher("/actuator/prometheus", "GET"));

    private final UserRepo userRepo;

    

    public JwtTokenFilter(SecurityConst securityConst, JwtTokenUtil jwtTokenUtil
            , UserRepo userRepo) {
        this.securityConst = securityConst;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepo = userRepo;
    }

    

    // 2. Implement shouldNotFilter to skip processing for public paths
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return excludedPaths.stream().anyMatch(p -> p.matches(request));
    }




    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @NotNull FilterChain chain)
            throws ServletException, IOException {

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
            resolver.resolveException(request, response, null, new InvalidBearerTokenException("Unauthorized"));
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
