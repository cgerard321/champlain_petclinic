package com.petclinic.auth.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
