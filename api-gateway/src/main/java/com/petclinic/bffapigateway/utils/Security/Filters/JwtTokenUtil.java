package com.petclinic.bffapigateway.utils.Security.Filters;

import com.petclinic.bffapigateway.exceptions.InvalidTokenException;
import com.petclinic.bffapigateway.exceptions.NoTokenFoundException;
import com.petclinic.bffapigateway.utils.Security.Variables.SecurityConst;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Generated;
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
@Generated
public class JwtTokenUtil implements Serializable {



    private final SecurityConst securityConst;
    private static final String CLAIM_KEY_ROLES = "roles";


    public List<String> getRolesFromToken(String token) {
        List<String> roles;
        try {
            final Claims claims = getClaimsFromToken(token);

           roles = Arrays.asList(claims.get(CLAIM_KEY_ROLES).toString().split(","));
        } catch (Exception e) {
            roles = null;
        }
        return roles;
    }




    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(securityConst.getSECRET())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }


    public String getTokenFromRequest(List<String> cookies){

        String[] allCookies = cookies.get(0).split(";");

        String token;

        token = Arrays.stream(allCookies).filter(cookie -> cookie.contains(securityConst.getTOKEN_PREFIX())).findFirst().orElseThrow(() -> new InvalidTokenException("Token is invalid"));



        token = token.replace(securityConst.getTOKEN_PREFIX()+"=", "");
        token = token.replace(";", "");
        token = token.replace(" ", "");

        return token;
    }


    public String getTokenFromRequest(ServerWebExchange exchange){
        final List<String> cookies = exchange.getRequest().getHeaders().get("Cookie");



        if (cookies == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

            throw new NoTokenFoundException("No cookies found");
        }

        String[] allCookies = cookies.get(0).split(";");

        String token;

        token = Arrays.stream(allCookies).filter(cookie -> cookie.contains(securityConst.getTOKEN_PREFIX())).findFirst().orElseThrow(() -> new InvalidTokenException("Token is invalid"));



        token = token.replace(securityConst.getTOKEN_PREFIX()+"=", "");
        token = token.replace(";", "");
        token = token.replace(" ", "");

        return token;
    }

    public String getIdFromToken(String token) {
        String id;
        try {
            final Claims claims = getClaimsFromToken(token);
            id = claims.get("id").toString();
        } catch (Exception e) {
            id = null;
        }
        return id;
    }
}
