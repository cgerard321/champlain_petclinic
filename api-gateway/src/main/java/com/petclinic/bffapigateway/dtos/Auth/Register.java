package com.petclinic.bffapigateway.dtos.Auth;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.PasswordStrengthCheck;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-15
 * Ticket: feat(APIG-CPC-354)
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Register {

    private String userId;

    private String email;
    private String username;
    @PasswordStrengthCheck
    private String password;

    private OwnerResponseDTO owner;

    public void setUserId(String usedId) {
        this.userId = usedId;
    }
}
