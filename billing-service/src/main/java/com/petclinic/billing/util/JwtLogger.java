package com.petclinic.billing.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Component
public class JwtLogger {

    @Value("${jwt.logging.enabled:false}")
    private boolean jwtLoggingEnabled;

    public void logJwt(String serviceName, String className, String methodName, String jwtToken) {
        if (!jwtLoggingEnabled || !log.isTraceEnabled()) return;

        if (jwtToken == null || jwtToken.isEmpty()) {
            log.trace("[{}:{}:{}] Missing or empty JWT token!", serviceName, className, methodName);
        } else {
            String hash = hashJwt(jwtToken);
            log.trace("[{}:{}:{}] JWT present=true, length={}, sha256={}",
                    serviceName, className, methodName, jwtToken.length(), hash);
        }
    }

    private String hashJwt(String jwtToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(jwtToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            return "hash_error";
        }
    }
}
