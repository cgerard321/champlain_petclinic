import { Routes } from '@angular/router';
import { Forbidden } from './pages/forbidden/forbidden';
import { NotFound } from './pages/not-found/not-found';

export default [
  { path: 'forbidden', component: Forbidden },
  { path: 'not-found', component: NotFound },
] satisfies Routes;
