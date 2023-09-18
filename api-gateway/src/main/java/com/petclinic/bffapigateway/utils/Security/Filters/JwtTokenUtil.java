package com.petclinic.bffapigateway.utils.Security.Filters;

import com.petclinic.bffapigateway.utils.Security.Variables.SecurityConst;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.io.Serializable;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUtil implements Serializable {



    private static final String CLAIM_KEY_ROLES = "roles";

    public String getUsernameFromToken(String token) {
        String username;
        try {
            final Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();


        } catch (Exception e) {
            username = null;
        }
        return username;
    }


    public List<String> getRolesFromToken(String token) {
        List<String> roles;
        try {
            final Claims claims = getClaimsFromToken(token);

           roles = Arrays.asList(claims.get(CLAIM_KEY_ROLES).toString().split(","));
            log.debug("Roles: {}", roles.toArray());
        } catch (Exception e) {
            log.debug("Exception in get Roles: {}", e.getMessage());
            roles = null;
        }
        return roles;
    }



    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            final Claims claims = getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            expiration = null;
        }
        return expiration;
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SecurityConst.SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.debug("Exception get claims: {}", e.getMessage());
            claims = null;
        }
        return claims;
    }


    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }



    public String getTokenFromRequest(ServerWebExchange exchange){
        log.debug("Entered Util getTokenFromRequest");
        final List<String> cookies = exchange.getRequest().getHeaders().get("Cookie");

        log.debug("Cookies: {}", cookies);

        if (cookies == null) {
            log.debug("No cookies found");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

            throw new RuntimeException("No cookies found");
        }

        String[] allCookies = cookies.get(0).split(",");

        String token;
        try {
            token = Arrays.stream(allCookies).filter(cookie -> cookie.contains("Bearer")).findFirst().orElseThrow(() -> new Exception("Token is invalid"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        token = token.replace("Bearer=", "");
        token = token.replace(";", "");

        return token;
    }
}
