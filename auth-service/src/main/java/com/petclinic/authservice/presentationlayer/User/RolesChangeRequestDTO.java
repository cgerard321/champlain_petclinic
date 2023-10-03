package com.petclinic.authservice.presentationlayer.User;

import com.petclinic.authservice.security.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class RolesChangeRequestDTO {
    List<String> roles;
}
