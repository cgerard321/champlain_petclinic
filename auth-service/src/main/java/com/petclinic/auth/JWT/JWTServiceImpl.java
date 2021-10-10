package com.petclinic.auth.JWT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.auth.Role.Role;
import com.petclinic.auth.User.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ObjectMapper objectMapper;

    public JWTServiceImpl(@Value("${jwt.expiration}") int expiration,
                          @Value("${jwt.secret}") String secret,
                          ObjectMapper objectMapper) {
        this.expiration = expiration;
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.objectMapper = objectMapper;
    }

    @Override
    public String encrypt(User user) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("roles", user.getRoles());

        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(claimsMap)
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

            final Claims body = claimsJws.getBody();
            final Set<Role> roles = (Set<Role>) body.get("roles", List.class)
                    .stream()
                    .map(n -> objectMapper.convertValue(n, Role.class))
                    .collect(Collectors.toSet());

            return User.builder()
                    .username(body.get("username", String.class))
                    .roles(roles)
                    .email(body.getSubject())
                    .build();

        } catch (JwtException ex) {
            ex.printStackTrace();
            //TODO: Add handling
            throw new RuntimeException("Something wrong with the JWT boss");
        }
    }
}
