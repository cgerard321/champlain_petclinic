package com.petclinic.authservice.presentationlayer.User;


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
