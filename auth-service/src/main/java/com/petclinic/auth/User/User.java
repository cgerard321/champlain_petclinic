package com.petclinic.auth.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.UniqueConstraint;

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

    @Column(unique = true)
    private String email;
}
