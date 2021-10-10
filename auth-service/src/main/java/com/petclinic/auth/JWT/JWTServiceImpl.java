package com.petclinic.auth.JWT;

import com.petclinic.auth.User.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: @Fube
 * Date: 2021-10-10
 * Ticket: feat(AUTH-CPC-357)
 */

@Service
public class JWTServiceImpl implements JWTService {

    private final int expiration;

    public JWTServiceImpl(@Value("${jwt.expiration}") int expiration) {
        this.expiration = expiration;
    }

    @Override
    public String encrypt(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256))
                .compact();
    }

    @Override
    public User decrypt(String token) {
        return null;
    }
}
