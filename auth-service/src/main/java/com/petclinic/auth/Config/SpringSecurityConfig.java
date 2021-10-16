/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-95)
 *
 * User: @Fube
 * Date: 24/10/21
 * Ticket: feat(AUTH-CPC-310)
 *
 * User: @Fube
 * Date: 2021-10-10
 * Ticket: feat(AUTH-CPC-357)
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.auth.Config;

import com.petclinic.auth.Exceptions.HTTPErrorMessage;
import com.petclinic.auth.User.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    private final static Pattern EXTRACT_FROM_SINGLE_QUOTES = Pattern.compile("(?<=')(?!\\s)[^']+(?<!\\s)(?=')");

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final UserRepo userRepo;
    private final JWTFilter jwtFilter;
    private final FilterExceptionHandler filterExceptionHandler;

    private static final String[] AUTH_WHITELIST = {
            // -- Swagger UI v2
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            // -- Swagger UI v3 (OpenAPI)
            "/v3/api-docs/**",
            "/swagger-ui/**"
            // other public endpoints of your API may be appended to this array
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        final Function<Exception, HTTPErrorMessage> duplicateEmailHandler = ex -> {
            final String message = ex.getMessage();
            String exMessage = message;

            if (message.contains("Duplicate")) {
                final ArrayList<String> strings = new ArrayList<>();
                final Matcher matcher = EXTRACT_FROM_SINGLE_QUOTES.matcher(message);
                while (matcher.find()) {
                    strings.add(matcher.group());
                }

                exMessage = format("%s %s is already in use", strings.get(1), strings.get(0));
            }

            return new HTTPErrorMessage(BAD_REQUEST.value(), exMessage);
        };
        filterExceptionHandler
                .registerHandler(SQLIntegrityConstraintViolationException.class, duplicateEmailHandler)
                .registerHandler(DataIntegrityViolationException.class, duplicateEmailHandler);

        http    .cors()
                .and()
                .csrf().disable()
                .authorizeRequests().antMatchers("/roles").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,"/users").permitAll()
                .antMatchers(HttpMethod.GET,"/users/*").permitAll()
                .antMatchers(HttpMethod.DELETE,"/users/*").permitAll()
                .antMatchers(HttpMethod.GET,"/users/verification/*").permitAll()
                .antMatchers(HttpMethod.POST,"/users/login").permitAll()
                .antMatchers(AUTH_WHITELIST).permitAll()
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(STATELESS)
                .and()
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        filterExceptionHandler,
                        JWTFilter.class
                );
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(username -> userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + username + "not found"))
        );
    }

    @Override @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
