/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @MaxGrabs
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-13)
 * <p>
 * User: @Trilikin21
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-64)
 * <p>
 * User: @JordanAlbayrak
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-102)
 * <p>
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.authservice.presentationlayer.User;

import com.petclinic.authservice.Util.Configuration.Security.PasswordStrengthCheck;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserIDLessRoleLessDTO {

    private String userId;
    private String username;

    @PasswordStrengthCheck
    private String password;

    private String email;
}