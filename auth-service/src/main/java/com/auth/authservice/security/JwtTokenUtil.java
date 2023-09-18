package com.auth.authservice.security;

import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.datalayer.user.UserRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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

    private final UserRepo userRepo;

    private static final long serialVersionUID = -5625635588908941275L;

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";
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
        ArrayList<String> roles = new ArrayList<>();
        user.getRoles().forEach(role -> roles.add(role.getName()));
        claims.put(CLAIM_KEY_ROLES,roles.toArray() );
        log.info("Claims are {}", claims.get(CLAIM_KEY_ROLES));
        log.info(user.getAuthorities().toString());
        log.info("Claims are {}", claims);

        return generateToken(claims);
    }
    
    String generateToken(Map<String, Object> claims) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());
        log.info("Expiration date is {}", expirationDate);
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, SecurityConst.SECRET)
                .compact();
    }


    public Boolean validateToken(String token) {
        final String username = getUsernameFromToken(token);
        if (username == null) return false;


        return (!isTokenExpired(token));
    }
}
