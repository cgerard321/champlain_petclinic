package com.auth.authservice.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SecurityConst {




    private long EXPIRATION_TIME_MINUTES;     // 1 hour
    private String SECRET;     // jwt secret
    private String TOKEN_PREFIX;     // Bearer


    public SecurityConst(@Value("${EXPIRATION_TIME_MINUTES}") long EXPIRATION_TIME_MINUTES, @Value("${SECRET_KEY}") String SECRET, @Value("${TOKEN_PREFIX}") String TOKEN_PREFIX) {
        this.EXPIRATION_TIME_MINUTES = EXPIRATION_TIME_MINUTES;
        this.SECRET = SECRET;
        this.TOKEN_PREFIX = TOKEN_PREFIX;
    }
}
