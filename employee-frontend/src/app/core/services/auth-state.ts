import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthState {
  private readonly _isLoggedIn = signal<boolean>(false);
  readonly isLoggedIn = this._isLoggedIn.asReadonly();

  setLoggedIn(value: boolean): void {
    this._isLoggedIn.set(value);
  }

  logout(): void {
    this._isLoggedIn.set(false);
  }
}
