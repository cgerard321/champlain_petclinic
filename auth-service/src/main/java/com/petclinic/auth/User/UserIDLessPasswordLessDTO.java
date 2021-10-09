package com.petclinic.auth.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserIDLessPasswordLessDTO {

    @NotEmpty
    private String username;

    @NotEmpty
    @Email(message = "Email must be valid")
    private String email;
}
