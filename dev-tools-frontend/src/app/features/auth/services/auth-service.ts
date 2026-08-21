import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthStateService } from '@core/services/auth-state-service';
import { LoginRequest } from '@features/auth/models/loginRequest';


@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authState = inject(AuthStateService);

  login(credentials: LoginRequest): Observable<void> {
    return this.http
      .post<void>('/login', credentials, { withCredentials: true })
      .pipe(tap(() => this.authState.markAuthenticated()));
  }
}
