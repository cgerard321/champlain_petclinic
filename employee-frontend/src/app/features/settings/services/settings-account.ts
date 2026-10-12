import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, of } from 'rxjs';

import { CurrentUserResponse } from '@core/models/current-user-response';
import { environment } from '@environments/environment';

const RESET_PASSWORD_PATH = '/users/reset-password/';

@Injectable({ providedIn: 'root' })
export class SettingsAccount {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/gateway/users';

  getCurrentUser(): Observable<CurrentUserResponse | null> {
    return this.http
      .get<CurrentUserResponse>(`${this.baseUrl}/jwt`)
      .pipe(catchError(() => of(null)));
  }

  updateUsername(userId: string, username: string): Observable<string> {
    return this.http.patch(`${this.baseUrl}/${userId}/username`, username, {
      headers: { 'Content-Type': 'text/plain' },
      responseType: 'text',
    });
  }

  sendPasswordResetLink(email: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/forgot_password`, {
      email,
      url: `${environment.customerPortal}${RESET_PASSWORD_PATH}`,
    });
  }
}
