/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Fube
 * Date: 09/10/21
 * Ticket: feat(AUTH-CPC-310)
 */
package com.petclinic.authservice.presentationlayer.User;

import com.petclinic.authservice.datalayer.roles.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserPasswordLessDTO {

    private String userId;


    private String username;

    private String email;

    private Set<Role> roles;
}
