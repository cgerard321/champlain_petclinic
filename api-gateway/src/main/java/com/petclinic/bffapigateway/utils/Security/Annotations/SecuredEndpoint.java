package com.petclinic.bffapigateway.utils.Security.Annotations;

import com.petclinic.bffapigateway.utils.Security.Variables.Roles;

import java.lang.annotation.*;


/**
 * <h1>Annotation for securing an endpoint with a list of roles.</h1>
 *
 *
 * <p>
 * Note : If All is specified, the endpoint will be accessible by all roles.
 * And if Anonymous is specified, the endpoint will be accessible by all roles and all non authenticated users.
 * Also if anonymous is specified, all other roles in the array will be ignored since everyone is allowed.
 *</p>
 * <p>
 * If this annotation is not present it will require a valid token but won't look for any roles, basically the default when
 * the annotation is not present is ALL
 * </p>
 *
 * <p>
 *  If no roles are specified ALL is default.
 *  </p>
 * @author Dylan Brassard
 * @since 2023-09-17
 *
 */

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredEndpoint {
    Roles[] allowedRoles() default {Roles.ALL};
}
