import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthState } from '@core/services/auth-state';


export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthState);
  const router = inject(Router);

  return auth.isAuthenticated() ? true : router.parseUrl('/login');
};
