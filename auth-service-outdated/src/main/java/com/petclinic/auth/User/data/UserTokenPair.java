package com.petclinic.auth.User.data;

import lombok.Builder;
import lombok.Data;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

@Data
@Builder(toBuilder = true)
public class UserTokenPair {
    private User user;
    private String token;
}
