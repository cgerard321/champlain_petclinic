/**
 * Created by IntelliJ IDEA.
 *
 * User: @MaxGrabs
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-13)
 *
 */
package com.petclinic.auth.Config;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//type class
@Target(ElementType.FIELD)
//validate during runtime
@Retention(RetentionPolicy.RUNTIME)
//constraint
@Constraint(validatedBy = PasswordValidation.class)
public @interface PasswordStrengthCheck {
    String message() default "Invalid Password, must be atleast 8 characters, have 1 digit, lower and upper case letters and a special character.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}