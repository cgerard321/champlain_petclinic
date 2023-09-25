package com.auth.authservice.presentationlayer.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResetPwdWithTokenRequestModel {
    private String password;
    private String token;
}
