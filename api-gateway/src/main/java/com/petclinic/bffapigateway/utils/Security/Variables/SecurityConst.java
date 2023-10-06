package com.petclinic.bffapigateway.utils.Security.Variables;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SecurityConst {



    private final long EXPIRATION_TIME_MINUTES;     // 1 hour
    private final String TOKEN_PREFIX;     // Bearer


    public SecurityConst(@Value("${EXPIRATION_TIME_MINUTES}") long EXPIRATION_TIME_MINUTES,@Value("${TOKEN_PREFIX}") String TOKEN_PREFIX) {
        this.EXPIRATION_TIME_MINUTES = EXPIRATION_TIME_MINUTES;
        this.TOKEN_PREFIX = TOKEN_PREFIX;
    }
}
