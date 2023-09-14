/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @MaxGrabs
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-13)
 */
package com.auth.authservice.Util.Configuration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class PasswordValidation implements ConstraintValidator<PasswordStrengthCheck, String> {

    @Override
    public void initialize(PasswordStrengthCheck constraintAnnotation) {
        // ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";


        return value != null && value.matches(pattern);
    }


}
