import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, map, Observable, of, tap } from 'rxjs';

import { CurrentUserResponse } from '@core/models/current-user-response';

@Injectable({ providedIn: 'root' })
export class AuthState {
  private readonly http = inject(HttpClient);

  private readonly _isAuthenticated = signal(false);
  readonly isAuthenticated = this._isAuthenticated.asReadonly();
  private readonly _roles = signal<string[]>([]);
  readonly roles = this._roles.asReadonly();

  logout(): Observable<void> {
    return this.http
      .post<void>('/api/gateway/users/logout', {})
      .pipe(tap(() => this._isAuthenticated.set(false)));
  }

  hasRole(role: string): boolean {
    return this._roles().includes(role);
  }

  checkToken(): Observable<void> {
    return this.http.get<CurrentUserResponse>('/api/gateway/users/jwt').pipe(
      tap((user) => {
        this._isAuthenticated.set(true);
        this._roles.set(user.roles);
      }),
      map(() => undefined),
      catchError((err: HttpErrorResponse) => {
        this._isAuthenticated.set(false);
        this._roles.set([]);
        if (err.status !== 401) {
          console.error('Unexpected error checking auth token', err);
        }
        return of(undefined);
      }),
    );
  }
}
