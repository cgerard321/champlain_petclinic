import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, switchMap, tap } from 'rxjs';
import { AuthState } from '@core/services/auth-state';
import { LoginRequest } from '@features/auth/models/loginRequest';


@Injectable({ providedIn: 'root' })
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly authState = inject(AuthState);

  login(credentials: LoginRequest): Observable<void> {
    return this.http
      .post<void>('/login', credentials, { withCredentials: true })
      .pipe(switchMap(() => this.authState.checkSession()));
  }
}
