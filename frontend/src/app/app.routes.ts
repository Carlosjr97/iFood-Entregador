import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/restricted-account/restricted-account').then((m) => m.RestrictedAccountPage),
  },
  { path: 'capture', loadComponent: () => import('./pages/capture/capture').then((m) => m.CapturePage) },
  { path: 'dashboard', loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.DashboardPage) },
  { path: 'about', loadComponent: () => import('./pages/about/about').then((m) => m.AboutPage) },
  { path: '**', redirectTo: '' },
];
