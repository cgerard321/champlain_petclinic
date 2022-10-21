package com.petclinic.auth.UserRole.data;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Table(schema = "auth", name = "users_roles")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty
    private int user_id;

    @NotEmpty
    private int role_id;
}
