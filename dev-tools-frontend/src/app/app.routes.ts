import { Routes } from '@angular/router';

import { authGuard } from '@core/guards/auth/auth-guard';
import { sudoGuard } from '@core/guards/sudo/sudo-guard';

export const routes: Routes = [
  {
    path: 'login',
    loadChildren: () => import('@features/auth/routes'),
  },
  {
    path: '',
    loadComponent: () => import('@layout/shell/shell').then((m) => m.Shell),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadChildren: () => import('@features/dashboard/routes'),
      },
      {
        path: 'logs',
        loadChildren: () => import('@features/docker-logs/routes'),
      },
      {
        path: 'db-console',
        loadChildren: () => import('@features/db-consoles/routes'),
      },
      {
        path: 'admin/users',
        loadChildren: () => import('@features/create-users/routes'),
        canActivate: [sudoGuard],
      },
    ],
  },
];
