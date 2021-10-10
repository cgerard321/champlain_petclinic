package com.petclinic.auth.JWT;

import com.petclinic.auth.User.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: @Fube
 * Date: 2021-10-10
 * Ticket: feat(AUTH-CPC-357)
 */

@Service
public class JWTServiceImpl implements JWTService {

    private final int expiration;
    private final Key key;

    public JWTServiceImpl(@Value("${jwt.expiration}") int expiration,
                          @Value("${jwt.secret}") String secret) {
        this.expiration = expiration;
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String encrypt(User user) {
        Map<String, Object> claimsMap = new HashMap<>();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    @Override
    public User decrypt(String token) {
        try {
            final Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            claimsJws.getBody().
            return new User();
        } catch (JwtException ex) {
            //TODO: Add handling
            throw new RuntimeException("Something wrong with the JWT boss");
        }
    }
}
