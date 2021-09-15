package com.petclinic.auth.User;

import com.petclinic.auth.Role.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Table(schema = "auth", name = "users")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {

    @Id
    private long id;

    private String username;
    private String password;
    private String email;

    private List<Role> roles;
}
