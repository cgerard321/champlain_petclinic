package com.petclinic.bffapigateway.utils.Security.Annotations;


import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.*;


/**
 * Annotation for securing an endpoint with a list of roles.
 *
 *
 * <p>
 * Note : If All is specified, the endpoint will be accessible by all roles.
 * And if Anonymous is specified, the endpoint will be accessible by all roles and all non authenticated users.
 * Also if anonymous is specified, all other roles will be ignored everyone is allowed.
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
