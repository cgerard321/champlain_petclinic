package com.petclinic.auth.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private long id;

    private String username;
    private String password;
    private String email;
}
