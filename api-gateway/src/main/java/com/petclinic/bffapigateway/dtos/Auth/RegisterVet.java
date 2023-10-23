package com.petclinic.bffapigateway.dtos.Auth;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.petclinic.bffapigateway.dtos.Vets.VetRequestDTO;
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
public class RegisterVet {
    private String userId;
    private String email;
    private String username;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final String defaultRole = Roles.VET.toString();
    @PasswordStrengthCheck
    private String password;
    private VetRequestDTO vet;
}
