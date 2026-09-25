import { Routes } from '@angular/router';

import { authGuard } from '@core/guards/auth/auth-guard';
import { employeeGuard } from '@core/guards/employee/employee-guard';
import { customerRedirectGuard } from '@core/guards/customers/customer-redirect';
import { Roles } from '@shared/models/roles';

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
        path: '',
        loadChildren: () => import('@features/home/routes').then((m) => m.routes),
      },
    ],
  },
  { path: '**', redirectTo: 'error/not-found' },
];
