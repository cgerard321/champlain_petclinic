import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthState {
  private readonly http = inject(HttpClient);

  private readonly _isAuthenticated = signal(false);
  readonly isAuthenticated = this._isAuthenticated.asReadonly();

  markAuthenticated(): void {
    this._isAuthenticated.set(true);
  }

  logout(): Observable<void> {
    return this.http
      .post<void>('/logout', {}, { withCredentials: true })
      .pipe(tap(() => this._isAuthenticated.set(false)));
  }

  checkSession(): Observable<void> {
    return this.http.get<void>('/session', { withCredentials: true }).pipe(
      tap(() => this._isAuthenticated.set(true)),
      map(() => undefined),
      catchError(() => {
        this._isAuthenticated.set(false);
        return of(undefined);
      }),
    );
  }
}
