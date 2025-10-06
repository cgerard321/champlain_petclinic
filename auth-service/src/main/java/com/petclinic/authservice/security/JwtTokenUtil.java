package com.petclinic.authservice.security;

import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUtil implements Serializable {



    private final SecurityConst securityConst;


    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";
    private static final String CLAIM_KEY_ROLES = "roles";
    private static final String CLAIM_KEY_ID = "id";




    public String getUsernameFromToken(String token) {
        String username;
        try {
            final Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();


        } catch (Exception e) {
            //SHOULD ADD THAT IT THROWS AN EXCEPTION, NOT JUST SETS IT NULL
            username = null;
        }
        return username;
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
            claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(securityConst.getSECRET().getBytes()))
                    .build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            log.info(e.getMessage());
            //SHOULD ADD THAT IT THROWS AN EXCEPTION, NOT JUST SETS IT NULL
            claims = null;
        }
        return claims;
    }


    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }


    public String generateToken(User user) {
        log.info("Generating token for user {}", user.getUsername());

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, user.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        claims.put(CLAIM_KEY_ROLES, user.getRoles().stream().map(Role::getName).toArray(String[]::new));
        claims.put(CLAIM_KEY_ID, user.getUserIdentifier().getUserId());
        log.info("Claims are {}", claims.get(CLAIM_KEY_ROLES));
        log.info(user.getAuthorities().toString());
        log.info("Claims are {}", claims);

        return generateToken(claims);
    }
    
    String generateToken(Map<String, Object> claims) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(securityConst.getEXPIRATION_TIME_MINUTES()).toInstant());
        log.info("Expiration date is {}", expirationDate);
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(securityConst.getSECRET().getBytes()))
                .compact();
    }


    public Boolean validateToken(String token) {
        final String username = getUsernameFromToken(token);
        if (username == null) return false;


        return (!isTokenExpired(token));
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
}
