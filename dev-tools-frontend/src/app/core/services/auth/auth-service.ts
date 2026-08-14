import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { LoginRequest } from '@core/models/auth/loginRequest';


@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly _isAuthenticated = signal(false);
  readonly isAuthenticated = this._isAuthenticated.asReadonly();

  login(credentials: LoginRequest): Observable<void> {
    return this.http
      .post<void>('/login', credentials, { withCredentials: true })
      .pipe(tap(() => this._isAuthenticated.set(true)));
  }

  logout(): Observable<void> {
    return this.http
      .post<void>('/logout', {}, { withCredentials: true })
      .pipe(tap(() => this._isAuthenticated.set(false)));
  }
}
