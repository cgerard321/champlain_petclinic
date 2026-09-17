import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthState } from '@core/services/auth-state';
import { EMPLOYEE_ROLES } from '@shared/models/employee-roles';

export const employeeGuard: CanActivateFn = () => {
  const auth = inject(AuthState);
  const router = inject(Router);

  const roles = auth.roles();
  const isEmployee = EMPLOYEE_ROLES.some((role) => roles.includes(role));

  return isEmployee || router.createUrlTree(['/error/forbidden']);
};
