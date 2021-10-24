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
public class UserIDLessUsernameLessDTO {

    @NotEmpty
    @Email(message = "Email must be valid")
    @Column(unique = true)
    private String email;

    @PasswordStrengthCheck
    private String password;
}
