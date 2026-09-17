import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthState } from '@core/services/auth-state';
import { environment } from '@environments/environment';

export const customerRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthState);

  if (auth.roles().includes('OWNER')) {
    window.location.href = environment.customerPortal;
    return false;
  }

  return true;
};
