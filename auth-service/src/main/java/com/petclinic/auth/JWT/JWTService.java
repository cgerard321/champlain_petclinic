package com.petclinic.auth.JWT;

import com.petclinic.auth.User.data.User;

/**
 * Created by IntelliJ IDEA.
 * User: @Fube
 * Date: 2021-10-10
 * Ticket: feat(AUTH-CPC-357)
 */
public interface JWTService {

    String encrypt(User user);
    User decrypt(String token);
}
