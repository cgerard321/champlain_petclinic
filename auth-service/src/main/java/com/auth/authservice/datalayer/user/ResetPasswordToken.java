package com.auth.authservice.datalayer.user;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name="reset_password_token")
@Data
public class ResetPasswordToken {
    private static final int EXPIRATION = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String token;

    private Long userIdentifier;

    private Date expiryDate;

    public ResetPasswordToken(Long userId, String token) {
        this.userIdentifier = userId;
        this.token = token;
        Calendar date = Calendar.getInstance();
        long timeInSecs = date.getTimeInMillis();
        Date expiryDate = new Date(timeInSecs + (EXPIRATION * 60 * 1000));
        this.expiryDate = expiryDate;
    }

    public ResetPasswordToken() {

    }
}
