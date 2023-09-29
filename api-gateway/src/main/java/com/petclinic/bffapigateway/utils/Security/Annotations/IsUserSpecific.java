package com.petclinic.bffapigateway.utils.Security.Annotations;

import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import jakarta.validation.Constraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Constraint(validatedBy = {})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IsUserSpecific {

    @Valid
    Roles[] bypassRoles() default {Roles.ADMIN};

    @NotEmpty
    @Valid
    String idToMatch();
}