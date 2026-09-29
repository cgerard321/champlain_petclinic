import {Component, inject} from '@angular/core';
import {Router} from '@angular/router';

@Component({
  imports: [],
  selector: 'app-forbidden',
  styleUrl: './forbidden.css',
  templateUrl: './forbidden.html',
})
export class Forbidden {
  private readonly router = inject(Router);
  // Uses tokens to redirect valid logged-in users to the home page,
  // while others are redirected to retry logging in
  get hasToken(): boolean {
    if (this.getToken()) {
      return true;
    } else {
      return false;
    }
  }

  handleAction(event: Event): void {
    event.preventDefault();

    if (this.isTokenValid()) {
      this.router.navigate(['/home']);
    } else {
      this.clearTokens();
      this.router.navigate(['/login']);
    }
  }

  private getToken(): string | null {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
  }

  private clearTokens(): void {
    localStorage.removeItem('token');
    sessionStorage.removeItem('token');
  }

  private isTokenValid(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const parts = token.split('.');

      if (parts.length !== 3) {
        return false;
      }

      // Gets the expiry date from the payload or assigns string so its never null
      const payloadBase64 = parts[1] || '';

      const decoded = JSON.parse(atob(payloadBase64));

      // JS time to JWT time conversion
      const currentTime = Math.floor(Date.now() / 1000);

      return decoded.exp > currentTime;
    } catch {
      return false;
    }
  }

}
