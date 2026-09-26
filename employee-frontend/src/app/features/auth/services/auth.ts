import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, switchMap } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { LoginRequest } from '@features/auth/models/loginRequest';

@Injectable({ providedIn: 'root' })
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly authState = inject(AuthState);

  login(credentials: LoginRequest): Observable<void> {
    return this.http
      .post<void>('/api/gateway/users/login', credentials)
      .pipe(switchMap(() => this.authState.checkToken()));
  }
}
