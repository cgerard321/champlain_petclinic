package com.petclinic.authservice.Util.Configuration.Security;


import com.petclinic.authservice.security.JwtTokenFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    UserDetailsService userDetailService;
    JwtTokenFilter jwtTokenFilter;
    CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;

    public SecurityConfig(UserDetailsService userDetailService, JwtTokenFilter jwtTokenFilter,
                          CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint) {
        this.userDetailService = userDetailService;
        this.jwtTokenFilter = jwtTokenFilter;
        this.customBasicAuthenticationEntryPoint = customBasicAuthenticationEntryPoint;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(encoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/actuator/prometheus")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET,"/users/verification/*")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/users/verification/**")).permitAll()
                // should skip filters
                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST,"/users/login")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/users/forgot_password")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/users/reset_password")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.HEAD,"/users")).authenticated()
                .requestMatchers(AntPathRequestMatcher.antMatcher( HttpMethod.POST,"/users")).permitAll()
                .anyRequest().authenticated())
                .logout(logout -> logout
                        .logoutUrl("/api/v1/users/logout")
                        .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) ->
                            httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT)
                        )
                        .addLogoutHandler((request, response, auth) -> {
                            for (Cookie cookie : request.getCookies()) {
                                String cookieName = cookie.getName();
                                Cookie cookieToDelete = new Cookie(cookieName, null);
                                cookieToDelete.setMaxAge(0);
                                response.addCookie(cookieToDelete);

                            }
                        }))
                .httpBasic()
                .authenticationEntryPoint(customBasicAuthenticationEntryPoint)
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        http.addFilterBefore(
                jwtTokenFilter,
                UsernamePasswordAuthenticationFilter.class
        );



        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin(Arrays.toString(new String[]{"http://localhost:3000","http://localhost:4200","http://localhost:9200"}));
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedHeader("*");
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
