package com.petclinic.authservice.presentationlayer.User;

import com.petclinic.authservice.Util.Configuration.Security.PasswordStrengthCheck;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResetPwdWithTokenRequestModel {

    @PasswordStrengthCheck
    private String password;
    private String token;
}
