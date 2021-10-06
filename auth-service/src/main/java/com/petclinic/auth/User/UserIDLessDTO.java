/**
 * Created by IntelliJ IDEA.
 *
 * User: @MaxGrabs
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-13)
 *
 * User: @Trilikin21
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-64)
 *
 * User: @JordanAlbayrak
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-102)
 *
 */
package com.petclinic.auth.User;

import com.petclinic.auth.Config.PasswordStrengthCheck;
import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserIDLessDTO {

    @NotEmpty
    private String username;

    @PasswordStrengthCheck
    private String password;

    @NotEmpty
    @Email(message = "Email must be valid")
    private String email;
}