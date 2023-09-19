package com.auth.authservice.security;

import org.springframework.stereotype.Component;

@Component
public class SecurityConst {


    public static long EXPIRATION_TIME_MINUTES = 60;     // 24 hours
    public static String SECRET = "mkcyetrmjcqLjOkwM08M676tl8LPnkgKHD2HlIpsYcSI8zGIiobE7yZ4N5JElvYMlTE8qqjTJ09JcqAsKdNxVA";     // jwt secret
    public static String TOKEN_PREFIX = "Bearer";

    // Token prefix
}
