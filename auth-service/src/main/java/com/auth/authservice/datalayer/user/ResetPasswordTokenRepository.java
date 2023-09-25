package com.auth.authservice.datalayer.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, Integer> {
    ResetPasswordToken findResetPasswordTokenByToken(String token);
    ResetPasswordToken findResetPasswordTokenByUserIdentifier(Long userId);
}
