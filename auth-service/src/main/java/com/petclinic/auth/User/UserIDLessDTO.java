package com.petclinic.auth.User;

import lombok.AllArgsConstructor;
        import lombok.Builder;
        import lombok.Data;
        import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserIDLessDTO {

    @NotEmpty
    @NotBlank
    @NotNull
    private String username;
    @NotEmpty
    @NotBlank
    @NotNull
    private String password;
    @NotEmpty
    @NotBlank
    @NotNull
    private String email;
}