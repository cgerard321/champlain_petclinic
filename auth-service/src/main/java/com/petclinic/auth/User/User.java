package com.petclinic.auth.User;

import com.petclinic.auth.Role.Role;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Table(schema = "auth", name = "users")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;
    private String password;
    private String email;

    @ManyToMany
    @JoinTable(
            schema = "auth",
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
