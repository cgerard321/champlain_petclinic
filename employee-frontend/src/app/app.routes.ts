import { Routes } from '@angular/router';

import { authGuard } from '@core/guards/auth/auth-guard';
import { employeeGuard } from '@core/guards/employee/employee-guard';
import { customerRedirectGuard } from '@core/guards/customers/customer-redirect';
import { Roles, EMPLOYEE_ROLES } from '@shared/models/roles';

export const routes: Routes = [
  {
    path: 'login',
    loadChildren: () => import('@features/auth/routes'),
  },
  {
    path: 'error',
    loadChildren: () => import('@features/error/routes'),
  },
  {
    path: '',
    loadComponent: () => import('@layout/shell/shell').then((m) => m.Shell),
    canActivate: [
      authGuard([Roles.admin, Roles.receptionist]),
      customerRedirectGuard,
      employeeGuard,
    ],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      {
        path: 'home',
        loadChildren: () => import('@features/home/routes'),
      },
      {
        path: 'inventory',
        canActivate: [authGuard(EMPLOYEE_ROLES)],
        loadChildren: () => import('@features/inventory/routes'),
      },
    ],
  },
  { path: '**', redirectTo: 'error/not-found' },
];
