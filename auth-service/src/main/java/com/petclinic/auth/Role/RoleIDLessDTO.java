package com.petclinic.auth.Role;

import lombok.*;

import javax.validation.constraints.NotEmpty;

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

    @NotEmpty
    private String name;

    private Role parent;
}
