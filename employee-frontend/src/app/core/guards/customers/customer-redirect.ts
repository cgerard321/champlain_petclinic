import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthState } from '@core/services/auth-state';
import { environment } from '@environments/environment';
import { CUSTOMER_ROLES } from '@shared/models/customer-roles';

export const customerRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthState);

  // This can accidently target a user with an employee role
  // We will keep it this way since no user should have an OWNER role if they are an employee
  const roles = auth.roles();
  const isCustomer = CUSTOMER_ROLES.some((role) => roles.includes(role));

  if (isCustomer) {
    window.location.href = environment.customerPortal;
    return false;
  }

  return true;
};
