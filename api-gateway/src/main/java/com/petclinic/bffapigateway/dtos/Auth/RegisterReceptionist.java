package com.petclinic.bffapigateway.dtos.Auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petclinic.bffapigateway.utils.Security.Annotations.PasswordStrengthCheck;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RegisterReceptionist {
    private String userId;
    private String email;
    private String username;
    @PasswordStrengthCheck
    private String password;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    protected final String defaultRole = Roles.RECEPTIONIST.toString();

}
