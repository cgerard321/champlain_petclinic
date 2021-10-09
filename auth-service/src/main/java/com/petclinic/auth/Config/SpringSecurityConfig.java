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
 */

package com.petclinic.auth.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http    .cors()
                .and()
                .csrf().disable()
                .authorizeRequests().antMatchers("/roles").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,"/users").permitAll()
                .anyRequest().authenticated();
    }
}
