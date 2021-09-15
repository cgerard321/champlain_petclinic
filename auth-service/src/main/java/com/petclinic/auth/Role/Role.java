package com.petclinic.auth.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(schema = "auth", name = "roles")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Role {

    public Role(long id, String name) {
        this.id = id;
        this.name = name;
        this.parent = null;
    }

    @Id
    private long id;
    private String name;

    @OneToMany
    @JoinTable
    private Role parent;
}
