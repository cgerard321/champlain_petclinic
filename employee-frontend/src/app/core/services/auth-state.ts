import { Injectable, signal } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthState {
  private readonly _isLoggedIn = signal<boolean>(false);
  readonly isLoggedIn = this._isLoggedIn.asReadonly();

  private readonly _roles = signal<string[]>([]);
  readonly roles = this._roles.asReadonly();

  setLoggedIn(value: boolean): void {
    this._isLoggedIn.set(value);
  }

  setRoles(roles: string[]): void {
    this._roles.set(roles);
  }

  isAuthenticated(): boolean {
    return this._isLoggedIn();
  }

  hasRole(role: string): boolean {
    return this._roles().includes(role);
  }

  checkToken(): Observable<boolean> {
    return of(this._isLoggedIn());
  }

  logout(): void {
    this._isLoggedIn.set(false);
    this._roles.set([]);
  }
}
