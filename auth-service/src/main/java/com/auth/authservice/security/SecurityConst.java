package com.auth.authservice.security;

import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;

public class SecurityConst {

    public static final long EXPIRATION_TIME = 60 * 60 * 1000;     // 24 hours
    public static final String SECRET = "mkcyetrmjcqLjOkwM08M676tl8LPnkgKHD2HlIpsYcSI8zGIiobE7yZ4N5JElvYMlTE8qqjTJ09JcqAsKdNxVA";     // jwt secret
    public static final String TOKEN_PREFIX = "Bearer";         // Token prefix
    public static final String HEADER_STRING = "Authorization"; // header key
}
