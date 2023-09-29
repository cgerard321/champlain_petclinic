package com.auth.authservice.presentationlayer.User;

import com.auth.authservice.datalayer.roles.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserDetails {


    private String userId;
    private String username;

    private String email;

    private Set<Role> roles;
}

