package com.petclinic.auth.Role;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RoleIDLessDTO {

    public RoleIDLessDTO(String name) {
        this.name = name;
        this.parent = null;
    }

    private String name;
    private Role parent;
}
