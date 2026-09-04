import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal, computed } from '@angular/core';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { RoleId } from '@shared/models/roles';
import { Apollo } from 'apollo-angular';

interface CurrentUserResponse {
  user_id: string;
  email: string;
  display_name: string;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthState {
  private readonly http = inject(HttpClient);

  private readonly _isAuthenticated = signal(false);
  readonly isAuthenticated = this._isAuthenticated.asReadonly();
  private readonly _roles = signal<string[]>([]);
  private readonly apollo = inject(Apollo);

  readonly isSudo = computed(() => this._roles().includes(RoleId.Sudo));

  markAuthenticated(): void {
    this._isAuthenticated.set(true);
  }

  logout(): Observable<void> {
    let _ = this.apollo.client.clearStore();

    return this.http
      .post<void>('/logout', {}, { withCredentials: true })
      .pipe(tap(() => this._isAuthenticated.set(false)));

  }

  checkSession(): Observable<void> {
    return this.http.get<CurrentUserResponse>('/session', { withCredentials: true }).pipe(
      tap((user) => {
        this._isAuthenticated.set(true);
        this._roles.set(user.roles);
      }),
      map(() => undefined),
      catchError(() => {
        this._isAuthenticated.set(false);
        this._roles.set([]);
        return of(undefined);
      }),
    );
  }
}
