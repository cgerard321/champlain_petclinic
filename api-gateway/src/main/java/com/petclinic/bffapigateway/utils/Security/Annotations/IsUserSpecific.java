package com.petclinic.bffapigateway.utils.Security.Annotations;

import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.lang.annotation.*;

/**
 * <h1>
 * Annotation for securing an endpoint to be accessible by a specific user only.
 *</h1>
 *
 * <p>
 * When present, this annotation will require a valid token and will check if the user id in the token matches the id specified in the annotation.
 * You specify which fields need to match in the idToMatch field.
 * This means that if you specify the idToMatch field as {"ownerId"} it will match the path variable called ownerId and the JWS id field.
 * You can add as many fields as you want to the idToMatch field, this will check if the JWS id matched any of the path variables.
 *
 * </p>
 *
 * <p>
 *  The bypass role field is used to specify which roles can bypass this annotation.
 *  This means if Vet is specified, any vet can access this endpoint, but any owners will need to be the concerned owner.
 *  If Admin is specified, any admin can access this endpoint, but any owners or vets will need to be the concerned owner.
 * </p>
 *
 * <p>
 *   WARNING : If you specify ANONYMOUS or ALL this annotation is redundant.
 * </p>
 *
 * <p>
 *  If no roles are specified ADMIN is default.
 *  </p>
 * @author Dylan Brassard
 * @since 2023-09-27
 * @see SecuredEndpoint
 */

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IsUserSpecific {

    @Valid
    Roles[] bypassRoles() default {Roles.ADMIN};

    @NotEmpty
    @Valid
    String[] idToMatch();
}