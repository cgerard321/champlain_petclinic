import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthState } from '@core/services/auth-state';
import { Roles } from '@shared/models/roles';

/**
 * The auth guard checks if the user is authenticated and has the required roles.
 * If the roles are not provided, the user is allowed to access the route.
 * If the user is not authenticated, they are redirected to the login page.
 * If the user does not have any of the required roles, they are redirected to the forbidden page.
 * @param allowedRoles
 */
export const authGuard =
  (allowedRoles?: Roles[]): CanActivateFn =>
  () => {
    const auth = inject(AuthState);
    const router = inject(Router);

    if (!auth.isAuthenticated()) {
      return router.parseUrl('/login');
    }

    if (!allowedRoles || allowedRoles.length === 0) {
      return true;
    }

    const hasRole = allowedRoles.some((role) => auth.hasRole(role));

    return hasRole || router.parseUrl('/error/forbidden');
  };
