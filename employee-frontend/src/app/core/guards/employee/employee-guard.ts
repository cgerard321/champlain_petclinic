import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthState } from '@core/services/auth-state';

const EMPLOYEE_ROLES = ['ADMIN', 'VET', 'RECEPTIONIST', 'INVENTORY_MANAGER'];

export const employeeGuard: CanActivateFn = () => {
  const auth = inject(AuthState);
  const router = inject(Router);

  const roles = auth.roles();
  const isEmployee = EMPLOYEE_ROLES.some((role) => roles.includes(role));

  return isEmployee || router.createUrlTree(['/error/forbidden']);
};
