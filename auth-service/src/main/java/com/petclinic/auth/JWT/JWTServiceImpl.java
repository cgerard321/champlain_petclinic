/**
 * Created by IntelliJ IDEA.
 * User: @Fube
 * Date: 2021-10-10
 * Ticket: feat(AUTH-CPC-357)
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.auth.JWT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.auth.Role.data.Role;
import com.petclinic.auth.User.data.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

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
        claimsMap.put("verified", user.isVerified());

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
        final Jws<Claims> claimsJws = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);

        final Claims body = claimsJws.getBody();
        Set<Role> roles;

        final User mappedUser = objectMapper.convertValue(body, User.class);

        final List<LinkedHashMap<String, String>> rolesList = body.get("roles", List.class);
        if(rolesList == null || rolesList.size() <= 0) {
            roles = Collections.emptySet();
        } else {
            roles = rolesList
                    .parallelStream()
                    .map(n -> objectMapper.convertValue(n, Role.class))
                    .collect(Collectors.toSet());
        }

        return mappedUser.toBuilder()
                .roles(roles)
                .email(body.getSubject())
                .build();

    }
}
