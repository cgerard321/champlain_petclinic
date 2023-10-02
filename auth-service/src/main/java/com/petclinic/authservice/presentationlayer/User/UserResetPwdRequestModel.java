package com.petclinic.authservice.presentationlayer.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResetPwdRequestModel {
    private String email;
    private String url;
}
