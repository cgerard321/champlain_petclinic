package com.petclinic.auth.Role;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Table(schema = "auth", name = "roles")
@Entity
@Getter
@Setter
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Role parent;
}
