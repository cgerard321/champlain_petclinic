package com.petclinic.bffapigateway.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility component responsible for conditionally logging JWT details.
 * <p>
 * When enabled via {@code jwt.logging.enabled=true}, this logger will:
 * <ul>
 *   <li>Log whether a JWT is present and its approximate length</li>
 *   <li>Include a SHA-256 hash of the token (never logs the token itself)</li>
 * </ul>
 * This ensures secure trace-level logging without exposing sensitive data.
 */
@Slf4j
@Component
public class JwtLogger {

    @Value("${jwt.logging.enabled:false}")
    private boolean jwtLoggingEnabled;

    /**
     * Logs basic JWT information if logging is enabled and trace level is active.
     *
     * @param serviceName name of the service calling the logger
     * @param className   name of the class making the log call
     * @param methodName  name of the method where the call originated
     * @param jwtToken    the JWT token value to analyze (never fully logged)
     */
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

    /**
     * Hashes the JWT token using SHA-256 and encodes it in Base64.
     *
     * @param jwtToken JWT token value
     * @return Base64-encoded SHA-256 hash or "hash_error" if hashing fails
     */
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
