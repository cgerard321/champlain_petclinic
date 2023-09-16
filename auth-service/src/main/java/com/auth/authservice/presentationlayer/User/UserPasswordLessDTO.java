/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Fube
 * Date: 09/10/21
 * Ticket: feat(AUTH-CPC-310)
 */
package com.auth.authservice.presentationlayer.User;

import com.auth.authservice.datalayer.roles.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
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

    private long id;

    private String username;


    private String email;

    private Set<Role> roles;
}
