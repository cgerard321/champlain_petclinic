import { Routes } from '@angular/router';

import { authGuard } from '@core/guards/auth/auth-guard';
import { customerRedirectGuard } from '@core/guards/customers/customer-redirect';
import { employeeGuard } from '@core/guards/employee/employee-guard';

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
    canActivate: [authGuard(), customerRedirectGuard, employeeGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      {
        path: 'home',
        loadChildren: () => import('@features/home/routes'),
      },
    ],
  },
  { path: '**', redirectTo: 'error/not-found' },
];
