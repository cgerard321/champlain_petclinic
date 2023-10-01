package com.petclinic.authservice.presentationlayer.User;

import com.petclinic.authservice.datalayer.user.User;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

@Data
@Builder(toBuilder = true)
@Setter
public class UserTokenPair {
    private User user;
    private String token;
}
