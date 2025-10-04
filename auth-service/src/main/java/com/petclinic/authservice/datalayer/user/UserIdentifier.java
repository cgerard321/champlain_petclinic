package com.petclinic.authservice.datalayer.user;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;


@Getter
@Embeddable
public class UserIdentifier {
    private String userId;

    public UserIdentifier() {
        this.userId = UUID.randomUUID().toString();
    }

    public UserIdentifier(String userId) {
        this.userId = userId;
    }

}
