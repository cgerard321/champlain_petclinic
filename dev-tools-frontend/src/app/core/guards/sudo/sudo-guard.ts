import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthState } from '@core/services/auth-state';

export const sudoGuard: CanActivateFn = () => {
  const auth = inject(AuthState);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.parseUrl('/login');
  }

  return auth.isSudo() ? true : router.parseUrl('/dashboard');
};
