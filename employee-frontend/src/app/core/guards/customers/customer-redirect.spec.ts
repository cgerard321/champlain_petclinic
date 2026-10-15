import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  provideRouter,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { vi } from 'vitest';

import { employeeGuard } from '@core/guards/employee/employee-guard';
import { AuthState } from '@core/services/auth-state';
import { environment } from '@environments/environment';
import { CUSTOMER_ROLES, EMPLOYEE_ROLES, Roles } from '@shared/models/roles';

import { customerRedirectGuard } from './customer-redirect';

describe('Customer role routing', () => {
  const roles = signal<string[]>([]);
  const employeeUrl = 'https://employee.example/settings';
  let location: { href: string };

  beforeEach(() => {
    roles.set([]);
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthState, useValue: { roles } }],
    });
    location = { href: employeeUrl };
    vi.stubGlobal('window', { location });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  function redirectCustomer(): ReturnType<CanActivateFn> {
    return TestBed.runInInjectionContext(() =>
      customerRedirectGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
  }

  function checkEmployee(): ReturnType<CanActivateFn> {
    return TestBed.runInInjectionContext(() =>
      employeeGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
  }

  it('recognizes CUST in the customer group and redirects to the existing portal', () => {
    roles.set(['CUST']);

    expect(CUSTOMER_ROLES).toContain('CUST');
    expect(Roles.customer).toBe('CUST');
    expect(redirectCustomer()).toBe(false);
    expect(location.href).toBe(environment.customerPortal);
    expect(checkEmployee()).toEqual(TestBed.inject(Router).createUrlTree(['/error/forbidden']));
  });

  it.each(EMPLOYEE_ROLES)('keeps %s on employee routes', (role) => {
    roles.set([role]);

    expect(redirectCustomer()).toBe(true);
    expect(location.href).toBe(employeeUrl);
    expect(checkEmployee()).toBe(true);
  });

  it('preserves customer redirection for an account with both customer and employee roles', () => {
    roles.set([Roles.customer, Roles.admin]);

    expect(redirectCustomer()).toBe(false);
    expect(location.href).toBe(environment.customerPortal);
  });

  it('does not redirect an account without roles and still denies employee access', () => {
    expect(redirectCustomer()).toBe(true);
    expect(location.href).toBe(employeeUrl);
    expect(checkEmployee()).toEqual(TestBed.inject(Router).createUrlTree(['/error/forbidden']));
  });
});
