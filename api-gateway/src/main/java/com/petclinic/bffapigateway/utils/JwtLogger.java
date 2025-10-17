package com.petclinic.bffapigateway.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtLogger {

    /**
     * Logs JWT presence and partial content for debugging.
     * @param serviceName The service or layer name (e.g., "Gateway", "Billing Controller", "Billing Service")
     * @param jwtToken The JWT token to log
     */
    public void logJwt(String serviceName,String className,String methodName, String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            log.warn("[{}] Missing or empty JWT token!", serviceName,":",className,":",methodName);
        } else {
            String shortened = jwtToken.length() > 25
                    ? jwtToken.substring(0, 25) + "..."
                    : jwtToken;
            log.debug("[{}] Received JWT: {}", serviceName,":",className,":",methodName, shortened);
        }
    }
}

