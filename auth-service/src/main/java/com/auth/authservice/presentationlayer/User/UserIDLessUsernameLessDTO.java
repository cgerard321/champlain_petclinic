package com.auth.authservice.presentationlayer.User;


import com.auth.authservice.Util.Configuration.PasswordStrengthCheck;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserIDLessUsernameLessDTO {


    private String email;

    private String password;
}
