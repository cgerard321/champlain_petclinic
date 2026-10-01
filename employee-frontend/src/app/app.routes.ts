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
    path: 'settings',
    loadChildren: () => import('@features/settings/routes'),
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

      {
        path: 'vets',
        loadChildren: () => import('@features/vets/routes'),
      },

      {
        path: 'cust',
        loadChildren: () => import('@features/cust/routes'),
      },

      {
        path: 'bill',
        loadChildren: () => import('@features/bill/routes'),
      },

      {
        path: 'vist',
        loadChildren: () => import('@features/vist/routes'),
      },

      {
        path: 'invt',
        loadChildren: () => import('@features/inventories/routes'),
      },

      {
        path: 'prod',
        loadChildren: () => import('@features/prod/routes'),
      },
      {
        path: 'error',
        loadChildren: () => import('@features/error/routes'),
      },
      {
        path: 'invt/:inventoryId/supplies',
        loadChildren: () => import('@features/supplies/routes'),
      },
    ],
  },
  { path: '**', redirectTo: 'error/not-found' },
];
