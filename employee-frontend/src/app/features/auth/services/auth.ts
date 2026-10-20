import { DOCUMENT } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, switchMap } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { ForgotPasswordRequest } from '@features/auth/models/forgotPasswordRequest';
import { LoginRequest } from '@features/auth/models/loginRequest';

const RESET_PASSWORD_PATH = '/login/reset-password/';

@Injectable({ providedIn: 'root' })
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly authState = inject(AuthState);
  private readonly document = inject(DOCUMENT);

  login(credentials: LoginRequest): Observable<void> {
    return this.http
      .post<void>('/api/gateway/users/login', credentials)
      .pipe(switchMap(() => this.authState.checkToken()));
  }

  sendPasswordResetEmail(email: string): Observable<void> {
    const body: ForgotPasswordRequest = {
      email,
      url: `${this.document.location.origin}${RESET_PASSWORD_PATH}`,
    };

    return this.http.post<void>('/api/gateway/users/forgot_password', body);
  }
}
