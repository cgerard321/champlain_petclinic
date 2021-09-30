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