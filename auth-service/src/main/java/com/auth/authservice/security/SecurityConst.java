package com.auth.authservice.security;

public class SecurityConst {
    public static final long EXPIRATION_TIME = 60 * 60 * 1000;     // 24 hours
    public static final String SECRET = "P@S5W0RD";      // jwt password
    public static final String TOKEN_PREFIX = "Bearer";         // Token prefix
    public static final String HEADER_STRING = "Authorization"; // header key
}
