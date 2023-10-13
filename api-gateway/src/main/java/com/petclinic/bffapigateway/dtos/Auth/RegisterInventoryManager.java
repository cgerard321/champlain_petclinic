package com.petclinic.bffapigateway.dtos.Auth;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.petclinic.bffapigateway.utils.Security.Annotations.PasswordStrengthCheck;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RegisterInventoryManager {
    private String userId;
    private String email;
    private String username;
    @PasswordStrengthCheck
    private String password;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    protected final String defaultRole = Roles.INVENTORY_MANAGER.toString();


}
