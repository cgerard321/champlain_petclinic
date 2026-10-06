import { HttpClient } from '@angular/common/http';
//imports to create an angular service
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { CurrentUserResponse } from '@core/models/current-user-response';
import { environment } from '@environments/environment';


const RESET_PASSWORD_PATH = '/users/reset-password/';


@Injectable({ providedIn: 'root' })
export class SettingsAccount {
  //private and cannot change these values
  //inject the client hyyp
  private readonly http = inject(HttpClient);
  //all calls will start by that
  private readonly baseUrl = '/api/gateway/users';

//get the current user
  getCurrentUser(): Observable<CurrentUserResponse> {
    return this.http.get<CurrentUserResponse>(`${this.baseUrl}/jwt`);
  }

  updateUsername(userId: string, username: string): Observable<string> {
    return this.http.patch(`${this.baseUrl}/${userId}/username`, username, {
      headers: { 'Content-Type': 'text/plain' },
      responseType: 'text',
    });
  }
//POST
  sendPasswordResetLink(email: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/forgot_password`, {
      email,
      url: `${environment.customerPortal}${RESET_PASSWORD_PATH}`,
    });
  }
}
