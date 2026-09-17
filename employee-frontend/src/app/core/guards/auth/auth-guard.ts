import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthState } from '@core/services/auth-state';
import { Roles } from '@shared/models/roles';

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
