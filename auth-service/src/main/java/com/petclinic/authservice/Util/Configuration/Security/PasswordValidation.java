/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @MaxGrabs
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-13)
 */
package com.petclinic.authservice.Util.Configuration.Security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PasswordValidation implements ConstraintValidator<PasswordStrengthCheck, String> {

    @Override
    public void initialize(PasswordStrengthCheck constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null) {
            return false;
        }
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=*!()_.<>,{}])(?=\\S+$).{8,}$";

        return value.matches(pattern);
    }

}
