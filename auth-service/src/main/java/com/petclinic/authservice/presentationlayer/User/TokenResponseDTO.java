package com.petclinic.authservice.presentationlayer.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class TokenResponseDTO {

        private String token;
        private String userId;
        private String email;
        private List<String> roles;

}
