package com.petclinic.bffapigateway.utils.Security.Variables;

import org.springframework.stereotype.Component;

@Component
public class SecurityConst {



    public static final long EXPIRATION_TIME_MINUTES = 60;     // 1 hour
    public static final String SECRET = "mkcyetrmjcqLjOkwM08M676tl8LPnkgKHD2HlIpsYcSI8zGIiobE7yZ4N5JElvYMlTE8qqjTJ09JcqAsKdNxVA";     // jwt secret
    public static final String TOKEN_PREFIX = "Bearer";


}
