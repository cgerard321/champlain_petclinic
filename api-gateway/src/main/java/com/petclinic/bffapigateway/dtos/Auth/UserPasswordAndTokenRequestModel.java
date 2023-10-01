package com.petclinic.bffapigateway.dtos.Auth;

import com.petclinic.bffapigateway.utils.Security.Annotations.PasswordStrengthCheck;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserPasswordAndTokenRequestModel {
    @PasswordStrengthCheck
    private String password;
    private String token;
}
